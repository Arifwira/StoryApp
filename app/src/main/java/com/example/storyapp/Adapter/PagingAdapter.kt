package com.example.storyapp.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.detail.DetailUser
import com.example.storyapp.story.ListStoryItem
import com.example.storyapp.utils.DateFormatter
import java.util.*
import kotlin.random.Random

class PagingAdapter :
    PagingDataAdapter<ListStoryItem, PagingAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("CheckResult")
        fun bind(data: ListStoryItem) {
            binding.tvItemName.text = data.name
            val url = data.photoUrl
            val randomValues = List(10) { Random.nextInt(0, 100) }
            val ava = "https://i.pravatar.cc/${randomValues[(0..9).random()]}"
            data.ava = ava
            Glide.with(itemView.context)
                .load(ava)
                .circleCrop()
                .into(binding.avatar)
            Glide.with(itemView.context)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar4.visibility = View.INVISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar4.visibility = View.INVISIBLE
                        return false
                    }
                })
                .into(binding.imgItemPhoto)
            binding.postDate.text = DateFormatter.formatDate(data.createdAt, TimeZone.getDefault().id)
            itemView.setOnClickListener {
                val optionCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.avatar, "ava"),
                        Pair(binding.postDate, "posted"),
                        Pair(binding.tvItemName, "uname")
                    )
                val intent = Intent(itemView.context, DetailUser::class.java)
                intent.putExtra("User", data)
                itemView.context.startActivity(intent, optionCompat.toBundle())
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}