package com.berfinilik.moviesappkotlin.ui.fragments

import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.FragmentDataProtectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class DataProtectionFragment : Fragment() {

    private var _binding: FragmentDataProtectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataProtectionBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.buttonDownloadData.setOnClickListener {
            downloadUserData()
        }

        return binding.root
    }

    private fun downloadUserData() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.data
                        if (userData != null) {
                            saveDataToFile(userData.toString())
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.data_not_found), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.data_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.data_download_fail), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveDataToFile(data: String) {
        try {
            val fileName = "user_data_${System.currentTimeMillis()}.json"
            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

            if (downloadsDir != null) {
                val file = File(downloadsDir, fileName)
                file.writeText(data)

                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(file.absolutePath),
                    null
                ) { path, uri ->
                    Toast.makeText(requireContext(), "Dosya kaydedildi: $path", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.data_download_fail), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error_occurred, e.localizedMessage), Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
