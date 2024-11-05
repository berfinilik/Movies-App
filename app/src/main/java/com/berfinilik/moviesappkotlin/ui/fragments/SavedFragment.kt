package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.adapters.SavedMoviesAdapter
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.databinding.FragmentSavedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SavedMoviesAdapter

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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val savedMovies = database.savedMoviesDao().getAllSavedMovies()
            withContext(Dispatchers.Main) {
                adapter.updateMovies(savedMovies)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SavedMoviesAdapter(emptyList())
        binding.savedRecyclerView.adapter = adapter
        binding.savedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
