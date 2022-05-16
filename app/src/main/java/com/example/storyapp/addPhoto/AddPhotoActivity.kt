package com.example.storyapp.addPhoto

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.RetrofitInstance
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityAddPhotoBinding
import com.example.storyapp.main.MainActivity
import com.example.storyapp.main.MainViewModel
import com.example.storyapp.story.StoryUploadResponse
import com.example.storyapp.user.UserPreference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPhotoBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(UserPreference.getInstance(dataStore), this)
    }

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        binding.uploadButton.setOnClickListener {
            mainViewModel.getUser().observe(this) {
                getMyLastLocation(it.token)
            }
        }
        binding.cameraXButton.setOnClickListener { startCameraX() }
        binding.galleryButton.setOnClickListener { startGallery() }
        playAnimation()
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private fun uploadImage(token: String, lat: Double, lon: Double) {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val text = binding.editTextTextMultiLine.text
            val description = "$text".toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            binding.darkBgupload.visibility = View.VISIBLE
            binding.progressBar5.visibility = View.VISIBLE
            val service =
                RetrofitInstance.getApiService()
            service.uploadImage(imageMultipart, description, lat, lon, "Bearer $token")
                .enqueue(object :
                    Callback<StoryUploadResponse> {
                    override fun onResponse(
                        call: Call<StoryUploadResponse>,
                        response: Response<StoryUploadResponse>
                    ) {
                        if (response.isSuccessful) {
                            binding.darkBgupload.visibility = View.INVISIBLE
                            binding.progressBar5.visibility = View.INVISIBLE
                            val responseBody = response.body()
                            if (responseBody != null && !responseBody.error) {
                                Toast.makeText(
                                    this@AddPhotoActivity,
                                    responseBody.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@AddPhotoActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            }
                        } else {
                            binding.darkBgupload.visibility = View.INVISIBLE
                            binding.progressBar5.visibility = View.INVISIBLE
                            Toast.makeText(
                                this@AddPhotoActivity,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<StoryUploadResponse>, t: Throwable) {
                        binding.darkBgupload.visibility = View.INVISIBLE
                        binding.progressBar5.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@AddPhotoActivity,
                            "Gagal instance Retrofit",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } else {
            Toast.makeText(
                this,
                "Silakan masukkan berkas gambar terlebih dahulu.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var getFile: File? = null
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            val result = rotateBitmap(
                BitmapFactory.decodeFile(getFile?.path),
                isBackCamera
            )

            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, this)

            getFile = myFile

            binding.previewImageView.setImageURI(selectedImg)
        }
    }

    private fun playAnimation() {
        val preview =
            ObjectAnimator.ofFloat(binding.previewImageView, View.ALPHA, 1f).setDuration(200)
        val galerry = ObjectAnimator.ofFloat(binding.galleryButton, View.ALPHA, 1f).setDuration(200)
        val camX = ObjectAnimator.ofFloat(binding.cameraXButton, View.ALPHA, 1f).setDuration(200)
        val description =
            ObjectAnimator.ofFloat(binding.editTextTextMultiLine, View.ALPHA, 1f).setDuration(200)
        val upload = ObjectAnimator.ofFloat(binding.uploadButton, View.ALPHA, 1f).setDuration(200)

        ObjectAnimator.ofFloat(binding.handBgAdd, View.TRANSLATION_Y, -50f, 50f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val together = AnimatorSet().apply {
            playTogether(galerry, camX)
        }
        AnimatorSet().apply {
            playSequentially(preview, together, description, upload)
            start()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    mainViewModel.getUser().observe(this) {
                        getMyLastLocation(it.token)
                    }
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    mainViewModel.getUser().observe(this) {
                        getMyLastLocation(it.token)
                    }
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation(token: String) {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    uploadImage(token, location.latitude, location.longitude)
                } else {
                    Toast.makeText(
                        this@AddPhotoActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}