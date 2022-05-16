package com.example.storyapp.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.Adapter.LoadingStateAdapter
import com.example.storyapp.Location.MapsActivity
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.addPhoto.AddPhotoActivity
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.login.LoginActivity
import com.example.storyapp.Adapter.PagingAdapter
import com.example.storyapp.user.UserModel
import com.example.storyapp.user.UserPreference

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var user: UserModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        binding.rvPaging.layoutManager =LinearLayoutManager(this)

        mainViewModel.getUser().observe(this){}

        getData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MAIN","PAUSE")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("MAIN","Restart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN","Resume")
    }

    private fun logout() {
        mainViewModel.logout()
        mainViewModel.getUser().observe(this) {
            println("Token Setelah Logout : " + it.token)
        }
        finish()
    }

    private fun getData() {
        val adapter = PagingAdapter()
        binding.rvPaging.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        mainViewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }

    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore),this)
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                this.user = user
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.location -> {
                startActivity(Intent(this@MainActivity, MapsActivity::class.java))
            }
            R.id.logout -> {
                logout()
            }
            R.id.setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.addStory -> {
                startActivity(Intent(this@MainActivity, AddPhotoActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}