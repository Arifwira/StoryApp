package com.example.storyapp.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.RegisterResponse
import com.example.storyapp.RetrofitInstance
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.user.UserBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {


    private val _story = MutableLiveData<List<ListStoryItem>>()
    val story: LiveData<List<ListStoryItem>> = _story
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    fun signup(name:String,email:String,pass:String) {
        _isLoading.value = true
        val retIn = RetrofitInstance.getApiService()
        val registerInfo = UserBody(name, email, pass)
        retIn.register(registerInfo).enqueue(object :
            Callback<RegisterResponse> {
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _success.value = false
                Log.d("YESSS", "GABISA")
            }
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.code() == 201) {
                    _isLoading.value = false
                    _success.value = true
                    Log.d("YESSS", "BISA")

                } else {
                    _isLoading.value = false
                    _success.value = false
                    Log.d("YESSS", "BISAGA")
                }
            }
        })
    }
}