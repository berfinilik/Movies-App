package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.berfinilik.moviesappkotlin.databinding.FragmentChangeUserNameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeUserNameFragment : Fragment() {

    private var _binding: FragmentChangeUserNameBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeUserNameBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadCurrentUserName()

        binding.backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonChangeUserName.setOnClickListener {
            updateUserName()
        }

        return binding.root
    }

    private fun loadCurrentUserName() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("userName")
                        binding.editTextUserName.setText(userName)
                    }
                }
                .addOnFailureListener {
                }
        }
    }

    private fun updateUserName() {
        val newUserName = binding.editTextUserName.text.toString().trim()
        if (newUserName.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen geçerli bir kullanıcı adı girin.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        userId?.let {
            val updates = mapOf("userName" to newUserName)

            firestore.collection("users").document(it)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Kullanıcı adı başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Kullanıcı adı güncellenirken hata oluştu.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
