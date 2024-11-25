package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berfinilik.moviesappkotlin.adapters.SavedMoviesAdapter
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.data.model.SavedMovie
import com.berfinilik.moviesappkotlin.databinding.FragmentSavedBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SavedMoviesAdapter
    private val selectedMovies = mutableSetOf<SavedMovie>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.deleteSelectedFab.setOnClickListener {
            if (selectedMovies.isEmpty()) {
                Snackbar.make(binding.root,"Kaydedilen listenizden film kaldırmak için lütfen önce bir veya daha fazla film seçin.",Snackbar.LENGTH_SHORT).show()
            } else {
                deleteSelectedMovies()
            }
        }

        loadSavedMovies()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val savedMovies = database.savedMoviesDao().getAllSavedMovies()
            withContext(Dispatchers.Main) {
                adapter.updateMovies(savedMovies)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SavedMoviesAdapter(
            emptyList(),
            onMovieClick = { movie ->
                val action = SavedFragmentDirections.actionSavedFragmentToDetailFragment(movie)
                findNavController().navigate(action)
            },
            onMovieSelected = { movie, isSelected ->
                if (isSelected) {
                    selectedMovies.add(movie)
                } else {
                    selectedMovies.remove(movie)
                }
            }
        )

        binding.savedRecyclerView.adapter = adapter
        binding.savedRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun deleteSelectedMovies() {
        if (selectedMovies.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Seçilen filmleri kaldırmak istediğinizden emin misiniz?")
            builder.setPositiveButton("Evet") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(requireContext())
                    selectedMovies.forEach { movie ->
                        database.savedMoviesDao().deleteSavedMovieById(movie.id)
                    }
                    selectedMovies.clear()
                    loadSavedMovies()
                }
            }
            builder.setNegativeButton("Hayır") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        } else {
            Snackbar.make(
                binding.root,
                "Kaydedilen listenizden film kaldırmak için lütfen önce bir veya daha fazla film seçin.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }



    private fun loadSavedMovies() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val savedMovies = database.savedMoviesDao().getAllSavedMovies()
            withContext(Dispatchers.Main) {
                if (savedMovies.isEmpty()) {
                    binding.emptyListTextView.visibility = View.VISIBLE
                    binding.savedRecyclerView.visibility = View.GONE
                    binding.deleteSelectedFab.visibility = View.GONE

                } else {
                    binding.emptyListTextView.visibility = View.GONE
                    binding.savedRecyclerView.visibility = View.VISIBLE
                    binding.deleteSelectedFab.visibility = View.VISIBLE
                    adapter.updateMovies(savedMovies)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
