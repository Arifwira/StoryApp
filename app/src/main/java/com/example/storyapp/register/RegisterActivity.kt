package com.example.storyapp.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.custom.CustomPassword
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.login.LoginActivity
import com.example.storyapp.user.UserPreference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var customPassword: CustomPassword
    private lateinit var registerButton: Button
    private lateinit var emailText: EditText
    private lateinit var nameText: EditText
    private lateinit var registerViewModel: RegisterViewModel

    private fun isValidEmail(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    private fun isValidPassword(str: String): Boolean {
        return str.length >= 6
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setupViewModel()
        registerButton = binding.registration
        customPassword = binding.editTextTextPassword2
        emailText = binding.editTextTextEmailAddress2
        nameText = binding.editTextTextPersonName

        nameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableButton()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
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


        registerViewModel.isLoading.observe(this) {
            showLoading(it)
        }
        binding.registration.setOnClickListener {
            val regEmail = binding.editTextTextEmailAddress2.text.toString()
            val regName = binding.editTextTextPersonName.text.toString()
            val regPass = binding.editTextTextPassword2.text.toString()
            registerViewModel.signup(regName, regEmail, regPass)
            registerViewModel.success.observe(this){
                if (it){
                    Toast.makeText(this,"REGISTRATION SUCCESS",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,LoginActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this,"ACCOUNT ALREADY EXIST",Toast.LENGTH_SHORT).show()
                }
            }
        }

        playAnimation()
    }

    private fun setupViewModel() {
        registerViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[RegisterViewModel::class.java]

    }

    private fun enableButton() {
        val name = nameText.text
        val email = emailText.text
        val pass = customPassword.text
        registerButton.isEnabled =
            pass != null && isValidPassword(pass.toString()) && email != null && isValidEmail(email.toString()) && name.isNotEmpty()
    }

    private fun playAnimation() {
        val register = ObjectAnimator.ofFloat(binding.textView2, View.ALPHA, 1f).setDuration(200)
        val name =
            ObjectAnimator.ofFloat(binding.editTextTextPersonName, View.ALPHA, 1f).setDuration(200)
        val email = ObjectAnimator.ofFloat(binding.editTextTextEmailAddress2, View.ALPHA, 1f)
            .setDuration(200)
        val password =
            ObjectAnimator.ofFloat(binding.editTextTextPassword2, View.ALPHA, 1f).setDuration(200)
        val registerButton =
            ObjectAnimator.ofFloat(binding.registration, View.ALPHA, 1f).setDuration(200)

        ObjectAnimator.ofFloat(binding.handbg2, View.TRANSLATION_Y, -50f, 50f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
        AnimatorSet().apply {
            playSequentially(register, name, email, password, registerButton)
            start()
        }
    }
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.darkBg2.visibility = View.VISIBLE
            binding.progressBar2.visibility = View.VISIBLE
        } else {
            binding.darkBg2.visibility = View.INVISIBLE
            binding.progressBar2.visibility = View.GONE
        }
    }
}