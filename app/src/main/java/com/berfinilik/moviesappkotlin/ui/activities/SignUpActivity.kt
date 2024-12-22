package com.berfinilik.moviesappkotlin.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.berfinilik.moviesappkotlin.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            val firstName = binding.editTextFirstName.text.toString().trim()
            val lastName = binding.editTextLastName.text.toString().trim()
            val userName = binding.editTextUserName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val reEnterPassword = binding.editTextPasswordConfirm.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || userName.isEmpty() || email.isEmpty() || password.isEmpty() || reEnterPassword.isEmpty()) {
                showSnackbar("Tüm alanları doldurunuz.")
                return@setOnClickListener
            }

            if (password != reEnterPassword) {
                showSnackbar("Şifreler eşleşmiyor!")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showSnackbar("Şifre en az 6 karakter olmalıdır.")
                return@setOnClickListener
            }

            createUserWithEmailAndPassword(email, password, firstName, lastName, userName)
        }

        binding.backIcon.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }


    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userName: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    showSnackbar("Kayıt başarılı. Giriş yapabilirsiniz.")

                    user?.let {
                        saveUserDataToFirestore(it, firstName, lastName, userName, email)
                    }
                } else {
                    val exception = task.exception
                    Log.w(TAG, "createUserWithEmail:failure", exception)
                    showSnackbar("Kayıt başarısız: ${exception?.message}")
                }
            }
    }

    private fun saveUserDataToFirestore(
        user: FirebaseUser,
        firstName: String,
        lastName: String,
        userName: String,
        email: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "userName" to userName,
            "email" to email
        )

        db.collection("users").document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d(TAG, "Firestore kaydı başarılı")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Belge eklerken hata oluştu", e)
            }
    }
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}