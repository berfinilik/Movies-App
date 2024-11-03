package com.berfinilik.moviesappkotlin

import android.app.Application
import androidx.room.Room
import com.berfinilik.moviesappkotlin.data.database.AppDatabase

class MyApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "movie-database"
        ).build()
    }
}
