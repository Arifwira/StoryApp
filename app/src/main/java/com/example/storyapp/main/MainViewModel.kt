package com.example.storyapp.main

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.RetrofitInstance
import com.example.storyapp.paging.StoryRepository
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.user.UserModel
import com.example.storyapp.user.UserPreference
import kotlinx.coroutines.launch


class MainViewModel(private val pref: UserPreference, storyRepository: StoryRepository) : ViewModel() {

    private val _story = MutableLiveData<List<ListStoryItem>>()
    val story: LiveData<List<ListStoryItem>> = _story
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val user = pref.getUser()
    val userModel = user.asLiveData()

    val stories: LiveData<PagingData<ListStoryItem>> =
        userModel.switchMap {
        storyRepository.getListStory("Bearer "+it.token)
            .cachedIn(viewModelScope)
        }

    suspend fun getLocation(token: String){
        _isLoading.value = true
        val responseData = RetrofitInstance.getApiService().getAllStory(20,1,token = "Bearer $token").listStory
        _story.value = responseData
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}