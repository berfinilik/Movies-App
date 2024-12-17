package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.berfinilik.moviesappkotlin.databinding.FragmentAccountInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AccountInfoFragment : Fragment() {

    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserInfo()

        binding.buttonUpdateUserInfo.setOnClickListener {
            updateUserInfo()
        }

        return binding.root


    }
    private fun loadUserInfo() {
        val userId=auth.currentUser?.uid
        if (userId!=null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener {document->
                if (document.exists()){
                    val userName=document.getString("userName")
                    val email=document.getString("email")

                    binding.editTextUserName.setText(userName)
                    binding.editTextEmail.setText(email)
                }
                else{
                    Toast.makeText(requireContext(),"Kullanıcı bilgisi bulunamadı",Toast.LENGTH_SHORT).show()
                }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Bilgiler yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show()

            }
        }
        else{
            Toast.makeText(requireContext(),"Kullanıcı bilgisi bulunamadı",Toast.LENGTH_SHORT).show()
        }

    }
    private fun updateUserInfo() {
        val userId=auth.currentUser?.uid
        val updatedUserName = binding.editTextUserName.text.toString().trim()
        val updatedEmail = binding.editTextEmail.text.toString().trim()

        if (updatedUserName.isEmpty() || updatedEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show()
            return
        }
        val updates = mapOf(
            "userName" to updatedUserName,
            "email" to updatedEmail
        )

        if (userId != null) {
            firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Bilgiler güncellenirken hata oluştu.", Toast.LENGTH_SHORT).show()
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}