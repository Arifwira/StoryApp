package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.RetrofitInstance
import com.example.storyapp.paging.PagingDatabase
import com.example.storyapp.paging.StoryRepository

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = PagingDatabase.getDatabase(context)
        val apiService = RetrofitInstance.getApiService()
        return StoryRepository(apiService)
    }

}