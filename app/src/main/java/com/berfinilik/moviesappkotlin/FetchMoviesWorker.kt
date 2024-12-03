package com.berfinilik.moviesappkotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.utils.KeyStore

class FetchMoviesWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private fun isNotificationEnabled(): Boolean {
        val sharedPreferences = applicationContext.getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_enabled", true)
    }

    override suspend fun doWork(): Result {
        if (!isNotificationEnabled()) {
            return Result.success()
        }

        try {
            val response = ApiClient.apiService.getDiscoverMovies(
                apiKey = getDecryptedApiKey() ?: return Result.failure(),
                sortBy = "release_date.desc"
            )

            if (response.isSuccessful && response.body() != null) {
                val movies = response.body()?.results?.take(3)
                movies?.forEach { movie ->
                    sendNotification(
                        title = "Yeni Eklenen Film!",
                        message = movie.title ?: ""
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }


    private fun getDecryptedApiKey(): String? {
        val sharedPreferences = applicationContext.getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE)
        val encryptedApiKey = sharedPreferences.getString("encryptedApiKey", null)
        val iv = sharedPreferences.getString("encryptionIv", null)
        return if (encryptedApiKey != null && iv != null) {
            val keyStore = KeyStore(applicationContext)
            keyStore.decryptData(encryptedApiKey, iv)
        } else {
            null
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "movies_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Movies Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}