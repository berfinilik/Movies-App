package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.PrivacySecurityAdapter
import com.berfinilik.moviesappkotlin.data.model.SettingAction
import com.berfinilik.moviesappkotlin.data.model.SettingOption
import com.berfinilik.moviesappkotlin.databinding.FragmentPrivacySecurityBinding

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

                else -> {}
            }
        }

        binding.recyclerViewPrivacySecurityOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPrivacySecurityOptions.adapter = adapter

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
