package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.SettingsAdapter
import com.berfinilik.moviesappkotlin.data.model.SettingAction
import com.berfinilik.moviesappkotlin.data.model.SettingOption
import com.berfinilik.moviesappkotlin.databinding.FragmentSettingsBinding
import com.berfinilik.moviesappkotlin.utils.ThemeHelper


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("theme_pref", 0)

        val settings = listOf(
            SettingOption(R.drawable.ic_theme, getString(R.string.theme_settings), SettingAction.CHANGE_THEME),
            SettingOption(R.drawable.ic_account_info, getString(R.string.account_info), SettingAction.ACCOUNT_INFO),
            SettingOption(R.drawable.ic_privacy_security, getString(R.string.privacy_and_security), SettingAction.PRIVACY_SECURITY),
            SettingOption(R.drawable.ic_notifications, getString(R.string.notification_settings), SettingAction.NOTIFICATIONS),
            SettingOption(R.drawable.ic_help_support, getString(R.string.help_and_support), SettingAction.HELP_SUPPORT)
        )



        val adapter = SettingsAdapter(settings) { option ->
            when (option.action) {
                SettingAction.CHANGE_THEME -> showThemeDialog()
                else -> {}
            }
        }

        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSettings.adapter = adapter

        return binding.root
    }

    private fun showThemeDialog() {
        val options = arrayOf("Açık Tema", "Karanlık Tema")
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        val selectedIndex = if (isDarkMode) 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle("Tema Ayarları")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val isDark = which == 1
                ThemeHelper.setThemeMode(isDark)
                sharedPreferences.edit().putBoolean("isDarkMode", isDark).apply()
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}