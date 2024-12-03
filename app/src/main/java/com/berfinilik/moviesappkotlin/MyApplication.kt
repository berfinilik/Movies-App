package com.berfinilik.moviesappkotlin

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.utils.KeyStore
import com.berfinilik.moviesappkotlin.utils.ThemeHelper
import java.util.concurrent.TimeUnit

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
        checkAndSetupWorkManager()
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

    private fun checkAndSetupWorkManager() {
        val sharedPreferences = getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        if (notificationsEnabled) {
            setupWorkManager()
        } else {
            cancelWorkManager()
        }
    }

    private fun setupWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<FetchMoviesWorker>(1, TimeUnit.DAYS)
            .addTag("FetchMoviesWork")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FetchMoviesWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelWorkManager() {
        WorkManager.getInstance(this).cancelUniqueWork("FetchMoviesWork")
    }
}