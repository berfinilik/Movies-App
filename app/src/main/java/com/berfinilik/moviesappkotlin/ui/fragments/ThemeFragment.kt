package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.utils.ThemeHelper

class ThemeFragment : Fragment() {

    private var selectedTheme: String = ThemeHelper.LIGHT_MODE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_theme, container, false)

        val radioGroupThemes = view.findViewById<RadioGroup>(R.id.radioGroupThemes)
        val btnSave = view.findViewById<View>(R.id.btnSaveTheme)

        val sharedPreferences = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val currentTheme = sharedPreferences.getString("theme", ThemeHelper.LIGHT_MODE)
        when (currentTheme) {
            ThemeHelper.LIGHT_MODE -> radioGroupThemes.check(R.id.radioLight)
            ThemeHelper.DARK_MODE -> radioGroupThemes.check(R.id.radioDark)
        }

        radioGroupThemes.setOnCheckedChangeListener { _, checkedId ->
            selectedTheme = when (checkedId) {
                R.id.radioLight -> ThemeHelper.LIGHT_MODE
                R.id.radioDark -> ThemeHelper.DARK_MODE
                else -> ThemeHelper.LIGHT_MODE
            }
        }

        btnSave.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putString("theme", selectedTheme).apply()
            ThemeHelper.applyTheme(selectedTheme)
            requireActivity().onBackPressed()
        }

        return view
    }
}
