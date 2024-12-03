package com.berfinilik.moviesappkotlin

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.utils.KeyStore
import com.berfinilik.moviesappkotlin.utils.ThemeHelper

class MyApplication : Application() {

    lateinit var keyStore: KeyStore

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "movie-database"
        ).build()
    }
    override fun onCreate() {
        super.onCreate()

        setupKeyStore()
        applyThemePreference()

    }

    private fun setupKeyStore() {
        keyStore = KeyStore(this)
        keyStore.createKey()
        val sharedPreferences = getSharedPreferences("SecurePrefs", MODE_PRIVATE)
        if (!sharedPreferences.contains("encryptedApiKey")) {
            val (encryptedApiKey, iv) = keyStore.encryptData("416ffe3feaa612800144e2d842faa042")
            sharedPreferences.edit().apply {
                putString("encryptedApiKey", encryptedApiKey)
                putString("encryptionIv", iv)
                apply()
            }
        }
    }

    private fun applyThemePreference() {
        val sharedPreferences = getSharedPreferences("theme_pref", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        ThemeHelper.setThemeMode(isDarkMode)
    }

}