package com.berfinilik.moviesappkotlin.ui.fragments

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.databinding.FragmentDataProtectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream


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
        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
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
                            saveDataToPdf(userData.toString()) // PDF Olarak Kaydet
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

    private fun saveDataToPdf(data: String) {
        try {
            val fileName = "user_data_${System.currentTimeMillis()}.pdf"
            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

            if (downloadsDir != null) {
                val file = File(downloadsDir, fileName)

                val document = Document()
                PdfWriter.getInstance(document, FileOutputStream(file))
                document.open()
                document.addAuthor("Movies App")
                document.addTitle("User Data")
                document.addCreationDate()
                document.addSubject("User Information")
                document.add(com.itextpdf.text.Paragraph(data))
                document.close()

                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(file.absolutePath),
                    null
                ) { path, uri ->
                    Toast.makeText(requireContext(), "PDF kaydedildi", Toast.LENGTH_SHORT).show()
                    openPdfFile(file)

                }

            } else {
                Toast.makeText(requireContext(), getString(R.string.data_download_fail), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error_occurred, e.localizedMessage), Toast.LENGTH_SHORT).show()
        }
    }
    private fun openPdfFile(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
