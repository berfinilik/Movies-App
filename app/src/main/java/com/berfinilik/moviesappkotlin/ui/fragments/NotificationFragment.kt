package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.berfinilik.moviesappkotlin.FetchMoviesWorker
import com.berfinilik.moviesappkotlin.R
import java.util.concurrent.TimeUnit

class NotificationFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isNotificationEnabled: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        isNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupNotifications)
        val btnSave = view.findViewById<Button>(R.id.btnSaveNotification)

        // Mevcut ayara göre seçim yap
        when (isNotificationEnabled) {
            true -> radioGroup.check(R.id.radioEnableNotifications)
            false -> radioGroup.check(R.id.radioDisableNotifications)
        }

        // RadioGroup seçim dinleyicisi
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            isNotificationEnabled = when (checkedId) {
                R.id.radioEnableNotifications -> true
                R.id.radioDisableNotifications -> false
                else -> true
            }
        }

        btnSave.setOnClickListener {
            saveNotificationPreference(isNotificationEnabled)
            handleNotificationWorkManager()
            requireActivity().onBackPressed()
        }

        return view
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
        val sharedPreferences = requireContext().getSharedPreferences("notification_pref", Context.MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("notification_enabled", true)

        if (isEnabled) {
            triggerNotificationWorkManager()
        } else {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag("FetchMoviesWork")
        }
    }
}
