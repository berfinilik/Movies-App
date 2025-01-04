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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 100
    private lateinit var callbackManager: CallbackManager



    companion object {
        private const val TAG = "LoginActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        requestNotificationPermission()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        setupGoogleSignIn()
        setupFacebookSignIn()


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
        binding.buttonGoogleLogin.setOnClickListener {
            showGoogleSignInDialog()
        }
        binding.buttonFacebookLogin.setOnClickListener {
            signInWithFacebook()
        }
    }
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }
    private fun showGoogleSignInDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Google ile Giriş Yap")
        dialog.setMessage("Google hesabınızla giriş yapmak istiyor musunuz?")
        dialog.setIcon(R.drawable.google) // Google logosu
        dialog.setPositiveButton("Evet") { _, _ ->
            signInWithGoogle()
        }
        dialog.setNegativeButton("İptal") { _, _ ->
            showSnackbar("Google giriş işlemi iptal edildi.")
        }
        dialog.create().show()
    }
    private fun setupFacebookSignIn() {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }
                override fun onCancel() {
                    showSnackbar("Facebook giriş işlemi iptal edildi.")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook Giriş Hatası: ${error.message}")
                    showSnackbar("Facebook ile giriş başarısız: ${error.message}")
                }
            }
        )
    }
    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserToFirestore(user!!)
                } else {
                    showSnackbar("Firebase kimlik doğrulama başarısız.")
                    Log.e(TAG, "Firebase Auth with Facebook: ${task.exception}")
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }
    private fun handleResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d(TAG, "Google Sign In successful: ${account.email}")
                val idToken = account.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    showSnackbar("Google kimlik doğrulama jetonu alınamadı.")
                    Log.e(TAG, "Google SignInAccount idToken is null")
                }
            } else {
                showSnackbar("Google hesabı bilgileri eksik.")
                Log.e(TAG, "Google SignInAccount is null")
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            showSnackbar("Google ile giriş başarısız: ${e.message}")
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserToFirestore(user!!)
                } else {
                    showSnackbar("Firebase kimlik doğrulama başarısız.")
                }
            }
    }
    private fun saveUserToFirestore(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)
        val displayName = user.displayName ?: "Bilinmiyor"
        val nameParts = displayName.split(" ")
        val firstName = nameParts.getOrNull(0) ?: "Bilinmiyor"
        val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else "Bilinmiyor"
        val userData = hashMapOf(
            "email" to (user.email ?: "Bilinmiyor"),
            "firstName" to firstName,
            "lastName" to lastName,
            "userName" to displayName
        )
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    Log.d(TAG, "Kullanıcı zaten Firestore'da kayıtlı.")
                    updateUI(user)
                } else {
                    userRef.set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Kullanıcı Firestore'a başarıyla kaydedildi.")
                            updateUI(user)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Kullanıcı Firestore'a kaydedilemedi.", e)
                            showSnackbar("Bir hata oluştu: ${e.message}")
                        }
                }
            } else {
                Log.w(TAG, "Firestore kullanıcısını kontrol ederken hata oluştu.", task.exception)
            }
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
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } ?: showSnackbar("Giriş yapılamadı.")
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