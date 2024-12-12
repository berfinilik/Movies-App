package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.PrivacySecurityAdapter
import com.berfinilik.moviesappkotlin.data.model.SettingAction
import com.berfinilik.moviesappkotlin.data.model.SettingOption
import com.berfinilik.moviesappkotlin.databinding.FragmentPrivacySecurityBinding
import com.berfinilik.moviesappkotlin.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PrivacySecurityFragment : Fragment() {

    private var _binding: FragmentPrivacySecurityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacySecurityBinding.inflate(inflater, container, false)

        val privacySettings = listOf(
            SettingOption(R.drawable.icon_password, getString(R.string.change_password), SettingAction.CHANGE_PASSWORD),
            SettingOption(R.drawable.ic_two_factor, getString(R.string.two_factor_auth), SettingAction.TWO_FACTOR_AUTH),
            SettingOption(R.drawable.ic_data_protection, getString(R.string.data_protection), SettingAction.DATA_PROTECTION),
            SettingOption(R.drawable.ic_delete_account, getString(R.string.delete_account),SettingAction.DELETE_ACCOUNT),
            SettingOption(R.drawable.ic_out, getString(R.string.logout), SettingAction.LOGOUT)
        )

        val adapter = PrivacySecurityAdapter(privacySettings) { option ->
            when (option.action) {
                SettingAction.DELETE_ACCOUNT -> showDeleteAccountDialog()
                SettingAction.LOGOUT -> confirmLogout()
                SettingAction.CHANGE_PASSWORD -> findNavController().navigate(R.id.action_privacySecurityFragment_to_changePasswordFragment)
                SettingAction.DATA_PROTECTION -> findNavController().navigate(R.id.action_privacySecurityFragment_to_dataProtectionFragment)


                else -> {}
            }
        }

        binding.recyclerViewPrivacySecurityOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPrivacySecurityOptions.adapter = adapter

        return binding.root
    }
    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(requireContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
    }


    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.delete_account_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                deleteAccount()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userId = user.uid
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), getString(R.string.account_deleted_successfully), Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.account_delete_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.account_delete_failed), Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
