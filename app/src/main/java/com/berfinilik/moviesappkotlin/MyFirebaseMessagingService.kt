package com.berfinilik.moviesappkotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            Log.d("FCM", "Bildirim Başlığı: ${it.title}, İçeriği: ${it.body}")
            showNotification(it.title ?: "Başlık Yok", it.body ?: "İçerik Yok")
        }

        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Veri Mesajı: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "Başlık Yok"
            val message = remoteMessage.data["message"] ?: "İçerik Yok"
            showNotification(title, message)
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Yeni FCM Token Alındı: $token")

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token Firestore'a başarıyla güncellendi.")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Token güncelleme hatası: ${e.localizedMessage}")
                }
        } ?: Log.e("FCM", "Kullanıcı oturumu açık değil, token güncellenemedi.")
    }


    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inactivity_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "İnaktif Kullanıcı Bildirimi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Uzun süre giriş yapılmayan kullanıcılar için bildirimler"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }


}