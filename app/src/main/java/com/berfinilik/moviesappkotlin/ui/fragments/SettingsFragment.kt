package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import com.berfinilik.moviesappkotlin.FetchMoviesWorker
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.SettingsAdapter
import com.berfinilik.moviesappkotlin.data.model.SettingAction
import com.berfinilik.moviesappkotlin.data.model.SettingOption
import com.berfinilik.moviesappkotlin.databinding.FragmentSettingsBinding
import com.berfinilik.moviesappkotlin.utils.ThemeHelper
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("theme_pref", Context.MODE_PRIVATE)

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
                SettingAction.NOTIFICATIONS -> showNotificationSettingsDialog()
                else -> {}
            }
        }

        binding.recyclerViewSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSettings.adapter = adapter

        return binding.root
    }

    private fun showNotificationSettingsDialog() {
        val sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        AlertDialog.Builder(requireContext())
            .setTitle("Bildirim Ayarları")
            .setSingleChoiceItems(arrayOf("Bildirimleri Aç", "Bildirimleri Kapat"), if (isEnabled) 0 else 1) { _, which ->
                saveNotificationPreference(which == 0)
            }
            .setPositiveButton("Kaydet") { dialog, _ ->
                dialog.dismiss()
                handleNotificationWorkManager()
            }
            .setNegativeButton("İptal", null)
            .show()
    }


    private fun saveNotificationPreference(enabled: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("notification_enabled", enabled)
            .apply()

        if (enabled) {
            scheduleWorkManager()
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork("FetchMoviesWork")
        }
    }

    private fun scheduleWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<FetchMoviesWorker>(1, TimeUnit.DAYS)
            .addTag("FetchMoviesWork")
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "FetchMoviesWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }


    private fun handleNotificationWorkManager() {
        val sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        if (isEnabled) {
            triggerNotificationWorkManager()
        } else {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag("FetchMoviesWork")
            Toast.makeText(requireContext(), "Bildirimler devre dışı bırakıldı.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNotificationEnabled(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_enabled", true)
    }


    private fun triggerNotificationWorkManager() {
        if (isNotificationEnabled()) {
            val workRequest = OneTimeWorkRequestBuilder<FetchMoviesWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        } else {
        }
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