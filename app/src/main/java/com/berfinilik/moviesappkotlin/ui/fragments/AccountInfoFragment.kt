package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.berfinilik.moviesappkotlin.R
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
    ): View {
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserInfo()

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.textViewChangeEmail.setOnClickListener {
            findNavController().navigate(R.id.action_accountInfoFragment_to_changeEmailFragment)
        }
        binding.textViewChangeUserName.setOnClickListener {
            findNavController().navigate(R.id.action_accountInfoFragment_to_changeUserNameFragment)
        }



        return binding.root
    }

    private fun loadUserInfo() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val userName = document.getString("userName") ?: ""
                        val email = document.getString("email") ?: ""

                        binding.textViewFirstName.setText(firstName)
                        binding.textViewLastName.setText(lastName)
                        binding.textViewUserName.setText(userName)
                        binding.textViewEmail.setText(email)
                    } else {
                    }
                }
                .addOnFailureListener {
                }
        } else {
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
