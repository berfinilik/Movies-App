package com.berfinilik.moviesappkotlin.ui.fragments

import LanguagePreferenceManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.MenuAdapter
import com.berfinilik.moviesappkotlin.data.model.MenuItem
import com.berfinilik.moviesappkotlin.databinding.BottomSheetProfilePictureBinding
import com.berfinilik.moviesappkotlin.databinding.FragmentAccountBinding
import com.berfinilik.moviesappkotlin.ui.activities.LoginActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var languagePreferenceManager: LanguagePreferenceManager


    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var photoUri: Uri? = null

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Kamera izni reddedildi", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uri ->
                    Glide.with(requireContext())
                        .load(uri)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.imgProfile)

                    uploadProfilePicture(uri)
                }
            } else {
                Toast.makeText(requireContext(), "Fotoğraf çekme iptal edildi", Toast.LENGTH_SHORT).show()
            }
        }


    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Glide.with(requireContext())
                    .load(it)
                    .circleCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.imgProfile)

                uploadProfilePicture(it)
            }
        }


    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        takePhotoLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir("Pictures")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languagePreferenceManager = LanguagePreferenceManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.recyclerGeneral.apply {
            adapter = MenuAdapter(getGeneralMenuItems()) { menuItem ->
                when (menuItem.title) {
                    getString(R.string.menu_theme) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_themeFragment)
                    }
                    getString(R.string.menu_notification) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_notificationFragment)
                    }
                    getString(R.string.menu_language) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_languageFragment)
                    }
                    getString(R.string.menu_data_protection) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_dataProtectionFragment)
                    }
                    getString(R.string.menu_about_app) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_aboutAppFragment)
                    }
                    getString(R.string.menu_delete_account) -> {
                        showDeleteAccountDialog()
                    }
                    getString(R.string.menu_logout) -> {
                        showLogoutDialog()
                    }
                    else -> {
                        Toast.makeText(requireContext(), menuItem.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.recyclerAccount.apply {
            adapter = MenuAdapter(getAccountMenuItems()) { menuItem ->
                when (menuItem.title) {
                    getString(R.string.menu_change_username) -> {
                        findNavController().navigate(R.id.action_accountInfoFragment_to_changeUserNameFragmentFragment)
                    }
                    getString(R.string.menu_change_password) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
                    }
                    getString(R.string.menu_change_email) -> {
                        findNavController().navigate(R.id.action_accountFragment_to_changeEmailFragment)
                    }

                    else -> {
                        Toast.makeText(requireContext(), menuItem.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_accountInfoFragment)
        }
        binding.imgProfile.setOnClickListener {
            showBottomSheet()
        }
        fetchUserDataFromFirestore()


        return view
    }

    private fun showLogoutDialog() {
        val dialogBinding = com.berfinilik.moviesappkotlin.databinding.DialogLogoutBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        val dialog = AlertDialog.Builder(requireContext(), R.style.CenteredDialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnLogout.setOnClickListener {
            performLogout()
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showDeleteAccountDialog() {
        val dialogBinding = com.berfinilik.moviesappkotlin.databinding.DialogDeleteAccountBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        val dialog = AlertDialog.Builder(requireContext(), R.style.CenteredDialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnYes.setOnClickListener {
            deleteAccount()
            dialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), getString(R.string.account_deleted_successfully), Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(requireActivity(), LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.account_delete_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.account_delete_failed), Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
        }
    }


    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    private fun getAccountMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(getString(R.string.change_username), R.drawable.ic_account_info),
            MenuItem(getString(R.string.menu_change_password), R.drawable.password),
            MenuItem(getString(R.string.menu_change_email), R.drawable.icon_email)
        )
    }

    private fun getGeneralMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(getString(R.string.menu_theme), R.drawable.ic_theme),
            MenuItem(getString(R.string.menu_notification), R.drawable.icon_notification),
            MenuItem(getString(R.string.menu_language), R.drawable.icon_language),
            MenuItem(getString(R.string.menu_data_protection), R.drawable.icon_data_protection),
            MenuItem(getString(R.string.menu_about_app), R.drawable.ic_about),
            MenuItem(getString(R.string.menu_delete_account), R.drawable.delete),
            MenuItem(getString(R.string.menu_logout), R.drawable.ic_logout)
        )
    }
    private fun fetchUserDataFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (isAdded && _binding != null) {
                        val firstName = document.getString("firstName") ?: "Ad"
                        val lastName = document.getString("lastName") ?: "Soyad"
                        val email = document.getString("email") ?: "E-posta Yok"
                        val profilePicture = document.getString("profilePicture")

                        binding.txtName.text = "$firstName $lastName"
                        binding.txtEmail.text = email

                        if (!profilePicture.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(profilePicture)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(binding.imgProfile)
                        } else {
                            binding.imgProfile.setImageResource(R.drawable.adduser)
                        }
                    }
                }
                .addOnFailureListener {
                    if (isAdded && _binding != null) {
                        binding.txtName.text = "Ad Soyad"
                        binding.txtEmail.text = "E-posta Yok"
                        binding.imgProfile.setImageResource(R.drawable.adduser)
                        Toast.makeText(requireContext(), "Kullanıcı verileri alınamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bindingBottomSheet = BottomSheetProfilePictureBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingBottomSheet.root)

        bindingBottomSheet.takePhotoButton.setOnClickListener {
            if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                Toast.makeText(requireContext(), "Cihazınızda kamera bulunmuyor", Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        bindingBottomSheet.selectPhotoButton.setOnClickListener {
            openGalleryLauncher.launch("image/*")
            bottomSheetDialog.dismiss()
        }

        bindingBottomSheet.viewPhotoButton.setOnClickListener {
            showFullScreenImage()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.show()
    }


    private fun showFullScreenImage(imageUrl: String? = null) {
        val user = auth.currentUser
        if (imageUrl != null) {
            val dialog = FullScreenImageDialogFragment(imageUrl)
            dialog.show(parentFragmentManager, "FullScreenImage")
        } else {
            user?.let {
                firestore.collection("users").document(it.uid).get()
                    .addOnSuccessListener { document ->
                        val storedImageUrl = document.getString("profilePicture")
                        if (!storedImageUrl.isNullOrEmpty()) {
                            val dialog = FullScreenImageDialogFragment(storedImageUrl)
                            dialog.show(parentFragmentManager, "FullScreenImage")
                        } else {
                            Toast.makeText(requireContext(), "Profil fotoğrafı bulunamadı", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Fotoğraf yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val user = auth.currentUser
        if (user != null) {
            val storageReference = storage.reference.child("profile_pictures/${user.uid}.jpg")
            Log.d("AccountFragment", "Fotoğraf yükleniyor: ${uri.path}")

            Glide.with(requireContext())
                .load(uri)
                .circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imgProfile)

            storageReference.putFile(uri)
                .addOnSuccessListener {
                    Log.d("AccountFragment", "Fotoğraf başarıyla yüklendi.")
                    storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                        Log.d("AccountFragment", "Download URL alındı: $downloadUrl")
                        updateProfilePictureInFirestore(downloadUrl)
                        showFullScreenImage(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AccountFragment", "Fotoğraf yükleme hatası: ${exception.localizedMessage}")
                    Toast.makeText(requireContext(), "Fotoğraf yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("AccountFragment", "Kullanıcı oturum açmamış!")
        }
    }

    private fun updateProfilePictureInFirestore(downloadUrl: Uri) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid)
                .set(mapOf("profilePicture" to downloadUrl.toString()), SetOptions.merge())
                .addOnSuccessListener {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Fotoğraf güncellendi", Toast.LENGTH_SHORT)
                            .show()

                        Glide.with(requireContext())
                            .load(downloadUrl)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(binding.imgProfile)
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && context != null) {
                        Log.e("Firestore", "Firestore güncelleme hatası: ${exception.localizedMessage}")
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

