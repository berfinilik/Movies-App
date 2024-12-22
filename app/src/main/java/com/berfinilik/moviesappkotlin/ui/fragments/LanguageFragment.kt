package com.berfinilik.moviesappkotlin.ui.fragments

import LanguagePreferenceManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.berfinilik.moviesappkotlin.databinding.FragmentLanguageBinding
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var languagePreferenceManager: LanguagePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languagePreferenceManager = LanguagePreferenceManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val savedLanguage = languagePreferenceManager.getSavedLanguage()
            when (savedLanguage) {
                "tr" -> binding.rbTurkish.isChecked = true
                "en" -> binding.rbEnglish.isChecked = true
            }
        }

        binding.btnSaveLanguage.setOnClickListener {
            val selectedLanguage = when {
                binding.rbTurkish.isChecked -> "tr"
                binding.rbEnglish.isChecked -> "en"
                else -> "tr"
            }
            setLocale(selectedLanguage)
            requireActivity().onBackPressed()

        }

        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        lifecycleScope.launch {
            languagePreferenceManager.saveLanguage(languageCode)
        }


        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
