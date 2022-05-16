package com.example.storyapp.detail

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityDetailUserBinding
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.utils.DateFormatter
import java.util.*

class DetailUser : AppCompatActivity() {
    private lateinit var binding: ActivityDetailUserBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hero = intent.getParcelableExtra<ListStoryItem>("User") as ListStoryItem

        binding.apply {
            username1.text = hero.name
            username2.text = hero.name
            description.text = hero.description
            date.text = DateFormatter.formatDate(hero.createdAt, TimeZone.getDefault().id)
        }
        Glide.with(this)
            .load(hero.ava)
            .circleCrop()
            .into(binding.avatarDetail)
        Glide.with(this)
            .load(hero.photoUrl)
            .into(binding.post)

    }

    companion object {
        var STORY = ""
        var AVATAR = ""
    }
}