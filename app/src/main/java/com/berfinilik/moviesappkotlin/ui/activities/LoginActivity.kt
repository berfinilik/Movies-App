package com.berfinilik.moviesappkotlin.ui.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "LoginActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestNotificationPermission()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        checkLastActive()

        binding.buttonLogin.setOnClickListener {
            val userName = binding.editTextName.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (userName.isNotEmpty() && password.isNotEmpty()) {
                findEmailByUserName(userName, password)
            } else {
                showSnackbar("Kullanıcı adı ve şifre boş olamaz!")
            }
        }
        binding.textViewResetPassword.setOnClickListener {
            val intent=Intent(this,ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun findEmailByUserName(userName: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("userName", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email") ?: ""
                    signInWithEmailAndPassword(email, password)
                } else {
                    showSnackbar("Kullanıcı adı bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Hata: ", e)
                showSnackbar("Bir hata oluştu.")
            }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateLastActiveAndFCMToken()
                    updateUI(user)

                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Kimlik doğrulama başarısız.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                Log.d(TAG, "Bildirim izni zaten verilmiş.")
            }
        }
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    private fun updateLastActiveAndFCMToken() {
        val user = auth.currentUser
        user?.let {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fcmToken = task.result
                        Log.d("FCM", "Token: $fcmToken")

                        val updates = mapOf(
                            "lastActive" to com.google.firebase.Timestamp.now(),
                            "fcmToken" to fcmToken
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(it.uid)
                            .update(updates)
                            .addOnSuccessListener {
                                Log.d(TAG, "Son aktif tarih ve FCM token güncellendi.")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "FCM token güncellenemedi: ${e.localizedMessage}")
                            }
                    } else {
                        Log.e(TAG, "FCM token alınamadı: ${task.exception?.localizedMessage}")
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(it.uid)
                            .update("lastActive", com.google.firebase.Timestamp.now())
                            .addOnSuccessListener {
                                Log.d("FCM", "Son aktif tarih güncellendi (fallback).")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Fallback güncelleme başarısız: ${e.localizedMessage}")
                            }
                    }
                }
        }
    }


    private fun checkLastActive() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d(TAG, "Kullanıcı giriş yapmamış, lastActive kontrolü yapılmadı.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val lastActive = document.getTimestamp("lastActive")?.toDate()?.time ?: 0L
                    val currentTime = System.currentTimeMillis()
                    val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000

                    if (currentTime - lastActive > sevenDaysInMillis) {
                        sendInactivityNotification()
                    } else {
                        Log.d(TAG, "Kullanıcı aktif durumda, bildirim gönderilmedi.")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "lastActive kontrolü başarısız: ${e.localizedMessage}")
            }
    }


    private fun sendInactivityNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inactivity_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "İnaktif Kullanıcı Bildirimi",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Film İzleme Vakti Gelmedi mi?")
            .setContentText("Uzun süredir aramızda yoksun. Yeni filmleri kaçırma!")
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }




    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Bildirim izni verildi.")
            } else {
                showSnackbar("Bildirim izni reddedildi. Bazı özellikler çalışmayabilir.")
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateLastActiveAndFCMToken()
            checkLastActive()
        } else {
            Log.d(TAG, "Oturum açık değil, `onResume` metodları çalıştırılmadı.")
        }
    }



}