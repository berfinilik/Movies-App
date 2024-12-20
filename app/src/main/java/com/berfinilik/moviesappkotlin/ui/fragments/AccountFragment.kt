package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.MenuAdapter
import com.berfinilik.moviesappkotlin.data.model.MenuItem
import com.berfinilik.moviesappkotlin.databinding.FragmentAccountBinding

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
                Toast.makeText(requireContext(), menuItem.title, Toast.LENGTH_SHORT).show()
            }
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.profile_edit), Toast.LENGTH_SHORT).show()
        }

        return view
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
