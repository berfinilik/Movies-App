package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.FragmentChangeEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailFragment : Fragment() {

    private var _binding: FragmentChangeEmailBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangeEmailBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        getCurrentEmail()

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonChangeEmail.setOnClickListener {
            showPasswordDialog()
        }

        return binding.root
    }

    private fun getCurrentEmail() {
        val user = auth.currentUser
        if (user != null) {
            val currentEmail = user.email
            if (currentEmail != null) {
                binding.textViewCurrentEmail.text = currentEmail
            }
        }
    }

    private fun showPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_password, null)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.editTextCurrentPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Mevcut Şifre")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val currentPassword = passwordEditText.text.toString()
                if (currentPassword.isNotEmpty()) {
                    reauthenticateAndChangeEmail(currentPassword)
                } else {
                    Toast.makeText(requireContext(), "Lütfen mevcut şifrenizi girin", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()
    }
    private fun reauthenticateAndChangeEmail(currentPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        val newEmail = binding.editTextNewEmail.text.toString()
                        val confirmEmail = binding.editTextConfirmNewEmail.text.toString()

                        if (validateEmailInput(newEmail, confirmEmail, user.email!!)) {
                            user.updateEmail(newEmail)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(requireContext(), getString(R.string.email_changed_successfully), Toast.LENGTH_SHORT).show()
                                        findNavController().navigateUp()
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.email_change_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        val errorMessage = reauthTask.exception?.message ?: "Bilinmeyen hata"
                        Toast.makeText(requireContext(), "Geçersiz şifre: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateEmailInput(newEmail: String, confirmEmail: String, currentEmail: String): Boolean {
        if (newEmail == currentEmail) {
            Toast.makeText(requireContext(), getString(R.string.new_email_same_as_current), Toast.LENGTH_SHORT).show()
            return false
        }
        if (newEmail.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.enter_new_email), Toast.LENGTH_SHORT).show()
            return false
        }

        if (confirmEmail.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.confirm_new_email), Toast.LENGTH_SHORT).show()
            return false
        }

        if (newEmail != confirmEmail) {
            Toast.makeText(requireContext(), getString(R.string.emails_do_not_match), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
