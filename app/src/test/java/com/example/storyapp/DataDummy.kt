package com.example.storyapp

import androidx.paging.PagingData
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.story.Story

object DataDummy {
    fun generateStoryItem(): List<ListStoryItem> {
        val listItem = mutableListOf<ListStoryItem>()
        repeat(10) {
            listItem.add(
                ListStoryItem(
                    ava = "https://i.pravatar.cc/$it",
                    photoUrl = "https://www.google-$it.com",
                    createdAt = "createdAt-$it",
                    name = "name-$it",
                    description = "desc-$it",
                    lon = it.toDouble(),
                    id = it.toString(),
                    lat = it.toDouble(),
                )
            )
        }
        return listItem
    }

    fun generatePagingDataStoryModel(): PagingData<ListStoryItem> = PagingData.from(
        generateStoryItem()
    )
}