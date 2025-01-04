package com.berfinilik.moviesappkotlin.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.FragmentFullScreenImageDialogBinding
import com.bumptech.glide.Glide

class FullScreenImageDialogFragment(private val imageUrl: String) : DialogFragment() {

    private var _binding: FragmentFullScreenImageDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullScreenImageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(imageUrl)
            .fitCenter()
            .into(binding.fullScreenImageView)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnShare.setOnClickListener {
            shareImage(imageUrl)
        }
    }

    private fun shareImage(imageUrl: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, imageUrl)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_prompt)))
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
