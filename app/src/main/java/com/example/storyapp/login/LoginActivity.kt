package com.example.storyapp.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.*
import com.example.storyapp.custom.CustomPassword
import com.example.storyapp.databinding.ActivityLoginBinding
import com.example.storyapp.main.MainActivity
import com.example.storyapp.register.RegisterActivity
import com.example.storyapp.user.User
import com.example.storyapp.user.UserModel
import com.example.storyapp.user.UserPreference
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    val TOKEN = "token"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var user: UserModel
    private lateinit var customPassword: CustomPassword
    private lateinit var loginButton: Button
    private lateinit var emailText: EditText
    private fun isValidEmail(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    private fun isValidPassword(str: String): Boolean {
        return str.length >= 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences(TOKEN, Context.MODE_PRIVATE)

        val intent = Intent(this, RegisterActivity::class.java)
        setupViewModel()

        loginButton = binding.login
        customPassword = binding.editTextTextPassword
        emailText = binding.editTextTextEmailAddress

        emailText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableButton()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        customPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableButton()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        signin()

        binding.register.setOnClickListener {
            startActivity(intent)
        }
        playAnimation()
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[LoginViewModel::class.java]


    }

    private fun signin() {
        binding.login.setOnClickListener {
            binding.emailWarn.visibility = View.INVISIBLE
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            val retIn = RetrofitInstance.getApiService()
            val signInInfo = User(email, password)
            val progressBar = binding.progressBar
            binding.darkBg.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            if (isValidEmail(email)) {
                if (isValidPassword(password)) {
                    retIn.login(signInInfo).enqueue(object : Callback<LoginResponse> {
                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            binding.darkBg.visibility = View.INVISIBLE
                            progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                this@LoginActivity,
                                t.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            println("LOGIN GAGAL NIH")
                        }

                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.code() == 200) {
                                binding.darkBg.visibility = View.INVISIBLE
                                progressBar.visibility = View.INVISIBLE
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login success!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                val responseBody = response.body()
                                val jsonObject =
                                    JSONTokener(responseBody?.loginResult.toString()).nextValue() as JSONObject
                                val tokenUser = jsonObject.getString("token")
                                loginViewModel.login()
                                loginViewModel.saveUser(UserModel(tokenUser, isLogin = true))
                                Log.d("Token Setelah Login ",tokenUser)
                                editor.putString(TOKEN, tokenUser)
                                editor.apply()
                                Log.d("YESSSSS", tokenUser)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login failed!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                binding.darkBg.visibility = View.INVISIBLE
                                progressBar.visibility = View.INVISIBLE
                                println("LOGIN GAGAL LOH")
                            }
                        }
                    })
                } else {
                    binding.darkBg.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(
                        this@LoginActivity,
                        "LOGIN FAILED, Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.d("LOGIN FAILED", "Password must be at least 6 characters")
                }
            } else {
                binding.darkBg.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                binding.emailWarn.visibility = View.VISIBLE
                Toast.makeText(
                    this@LoginActivity,
                    "LOGIN FAILED, Email not valid",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("LOGIN FAILED", "Email not valid")
            }
        }
    }

    private fun enableButton() {
        val email = emailText.text
        val pass = customPassword.text
        loginButton.isEnabled =
            pass != null && isValidPassword(pass.toString()) && email != null && isValidEmail(email.toString())
    }

    private fun playAnimation() {
        val welcome = ObjectAnimator.ofFloat(binding.imageView2, View.ALPHA, 1f).setDuration(200)
        val leggo = ObjectAnimator.ofFloat(binding.textView, View.ALPHA, 1f).setDuration(200)
        val email = ObjectAnimator.ofFloat(binding.editTextTextEmailAddress, View.ALPHA, 1f)
            .setDuration(200)
        val password =
            ObjectAnimator.ofFloat(binding.editTextTextPassword, View.ALPHA, 1f).setDuration(200)
        val login = ObjectAnimator.ofFloat(binding.login, View.ALPHA, 1f).setDuration(200)
        val register = ObjectAnimator.ofFloat(binding.register, View.ALPHA, 1f).setDuration(200)

        ObjectAnimator.ofFloat(binding.handbg, View.TRANSLATION_Y, -50f, 50f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        AnimatorSet().apply {
            playSequentially(welcome, leggo, email, password, login, register)
            start()
        }
    }
}