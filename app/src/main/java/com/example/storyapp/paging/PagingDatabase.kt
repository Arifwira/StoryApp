package com.example.storyapp.paging

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.storyapp.story.ListStoryItem

@Database(
    entities = [ListStoryItem::class],
    version = 1,
    exportSchema = false
)
abstract class PagingDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: PagingDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): PagingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PagingDatabase::class.java, "paging_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}