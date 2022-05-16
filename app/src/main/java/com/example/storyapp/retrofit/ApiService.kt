package com.example.storyapp

import com.example.storyapp.story.StoryResponse
import com.example.storyapp.story.StoryUploadResponse
import com.example.storyapp.user.User
import com.example.storyapp.user.UserBody
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface ApiService {

    @POST("v1/login")
    fun login(
        @Body user: User
    ): Call<LoginResponse>

    @POST("v1/register")
    fun register(
        @Body info: UserBody
    ): Call<RegisterResponse>

    @GET("v1/stories")
    fun getStory(
        @Query("size") size: Int,
        @Query("page") page: Int,
        @Query("location") location: Int = 1,
        @Header("Authorization") token: String
    ): Call<StoryResponse>

    @GET("v1/stories")
    suspend fun getAllStory(
        @Query("size") size: Int,
        @Query("page") page : Int,
        @Query("location") location: Int = 1,
        @Header("Authorization") token: String
    ): StoryResponse

    @Multipart
    @POST("/v1/stories")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double? = null,
        @Part("lon") lon: Double? = null,
        @Header("Authorization") token: String
    ): Call<StoryUploadResponse>

}

data class RegisterResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,
)

data class LoginResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("loginResult")
    val loginResult: JsonObject
)

class RetrofitInstance {
    companion object{
        fun getApiService(): ApiService {
            val loggingInterceptor =
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://story-api.dicoding.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}


