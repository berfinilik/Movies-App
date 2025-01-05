package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.*
import com.berfinilik.moviesappkotlin.FetchMoviesWorker
import com.berfinilik.moviesappkotlin.databinding.FragmentNotificationBinding
import java.util.concurrent.TimeUnit

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var isNotificationEnabled: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        isNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        // Bildirim Durumunu Kontrol Et ve UI Güncelle
        when (isNotificationEnabled) {
            true -> binding.radioGroupNotifications.check(binding.radioEnableNotifications.id)
            false -> binding.radioGroupNotifications.check(binding.radioDisableNotifications.id)
        }

        // RadioGroup Değişim Dinleyicisi
        binding.radioGroupNotifications.setOnCheckedChangeListener { _, checkedId ->
            isNotificationEnabled = when (checkedId) {
                binding.radioEnableNotifications.id -> true
                binding.radioDisableNotifications.id -> false
                else -> true
            }
        }

        // Kaydet Butonu
        binding.btnSaveNotification.setOnClickListener {
            saveNotificationPreference(isNotificationEnabled)
            handleNotificationWorkManager()
            requireActivity().onBackPressed()
        }

        // Geri Butonu
        binding.backIcon.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("notification_enabled", enabled)
            .apply()

        if (enabled) {
            scheduleWorkManager()
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork("FetchMoviesWork")
        }
    }

    private fun triggerNotificationWorkManager() {
        if (isNotificationEnabled) {
            val workRequest = OneTimeWorkRequestBuilder<FetchMoviesWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(workRequest)
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
        val isEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        if (isEnabled) {
            triggerNotificationWorkManager()
        } else {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag("FetchMoviesWork")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
