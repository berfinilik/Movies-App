package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.MenuAdapter
import com.berfinilik.moviesappkotlin.data.model.MenuItem
import com.berfinilik.moviesappkotlin.databinding.FragmentAccountBinding
import com.berfinilik.moviesappkotlin.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.recyclerGeneral.apply {
            adapter = MenuAdapter(getGeneralMenuItems()) { menuItem ->
                when (menuItem.title) {
                    getString(R.string.menu_theme) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_themeFragment)
                    }
                    getString(R.string.menu_notification) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_notificationFragment)
                    }
                    getString(R.string.menu_delete_account) -> {
                        showDeleteAccountDialog()
                    }
                    getString(R.string.menu_logout) -> {
                        showLogoutDialog()
                    }
                    else -> {
                        Toast.makeText(requireContext(), menuItem.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.recyclerAccount.apply {
            adapter = MenuAdapter(getAccountMenuItems()) { menuItem ->
                when (menuItem.title) {
                    getString(R.string.menu_change_password) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
                    }
                    getString(R.string.menu_change_email) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_changeEmailFragment)
                    }

                    else -> {
                        Toast.makeText(requireContext(), menuItem.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.profile_edit), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showLogoutDialog() {
        val dialogBinding = com.berfinilik.moviesappkotlin.databinding.DialogLogoutBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        val dialog = AlertDialog.Builder(requireContext(), R.style.CenteredDialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnLogout.setOnClickListener {
            performLogout()
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showDeleteAccountDialog() {
        val dialogBinding = com.berfinilik.moviesappkotlin.databinding.DialogDeleteAccountBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        val dialog = AlertDialog.Builder(requireContext(), R.style.CenteredDialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnYes.setOnClickListener {
            deleteAccount()
            dialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
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
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(requireActivity(), LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
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


    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    private fun getAccountMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(getString(R.string.menu_change_password), R.drawable.password),
            MenuItem(getString(R.string.menu_change_email), R.drawable.icon_email)
        )
    }

    private fun getGeneralMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(getString(R.string.menu_theme), R.drawable.ic_theme),
            MenuItem(getString(R.string.menu_notification), R.drawable.icon_notification),
            MenuItem(getString(R.string.menu_language), R.drawable.icon_language),
            MenuItem(getString(R.string.menu_data_protection), R.drawable.icon_data_protection),
            MenuItem(getString(R.string.menu_about_app), R.drawable.ic_about),
            MenuItem(getString(R.string.menu_delete_account), R.drawable.delete),
            MenuItem(getString(R.string.menu_logout), R.drawable.ic_logout)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
