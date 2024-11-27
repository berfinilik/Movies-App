package com.berfinilik.moviesappkotlin.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.berfinilik.moviesappkotlin.databinding.ActivityForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding:ActivityForgotPasswordBinding
    private lateinit var auth:FirebaseAuth
    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        binding.buttonForgotPassword.setOnClickListener {
            val userName = binding.editTextForgotUserName.text.toString().trim()


            if (userName.isNotEmpty()) {
                resetPassword(userName)
            } else {
                showSnackbar("Kullanıcı adı boş olamaz!")
            }
        }
        binding.textViewSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun resetPassword(userName: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("userName", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email") ?: ""
                    sendPasswordResetEmail(email)
                } else {
                    showSnackbar("Kullanıcı adı bulunamadı.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Hata: ", e)
                showSnackbar("Bir hata oluştu.")
            }
    }
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSnackbar("Şifre sıfırlama e-postası gönderildi.")
                } else {
                    showSnackbar("E-posta gönderilemedi: ${task.exception?.message}")
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}