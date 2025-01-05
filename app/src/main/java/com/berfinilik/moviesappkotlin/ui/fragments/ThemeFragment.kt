package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.berfinilik.moviesappkotlin.databinding.FragmentThemeBinding
import com.berfinilik.moviesappkotlin.utils.ThemeHelper

class ThemeFragment : Fragment() {

    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!

    private var selectedTheme: String = ThemeHelper.LIGHT_MODE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeBinding.inflate(inflater, container, false)
        val view = binding.root

        val sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val currentTheme = sharedPreferences.getString("theme", ThemeHelper.LIGHT_MODE)
        when (currentTheme) {
            ThemeHelper.LIGHT_MODE -> binding.radioGroupThemes.check(binding.radioLight.id)
            ThemeHelper.DARK_MODE -> binding.radioGroupThemes.check(binding.radioDark.id)
        }

        binding.radioGroupThemes.setOnCheckedChangeListener { _, checkedId ->
            selectedTheme = when (checkedId) {
                binding.radioLight.id -> ThemeHelper.LIGHT_MODE
                binding.radioDark.id -> ThemeHelper.DARK_MODE
                else -> ThemeHelper.LIGHT_MODE
            }
        }

        binding.btnSaveTheme.setOnClickListener {
            sharedPreferences.edit()
                .putString("theme", selectedTheme)
                .apply()

            ThemeHelper.applyTheme(selectedTheme)
            requireActivity().onBackPressed()
        }

        binding.backIcon.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
