package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berfinilik.moviesappkotlin.adapters.FavoritesAdapter
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.databinding.FragmentFavouritesBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavouritesFragment : Fragment() {

    private val movieViewModel: MovieViewModel by viewModels()

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFavorites()

        binding.fabViewAll.setOnClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToAllPopularMoviesFragment()
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return@launch
            val favoriteMovies = database.favouritesDao().getAll(userId)
            withContext(Dispatchers.Main) {
                favoritesAdapter.updateData(favoriteMovies)
            }
        }
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter(emptyList()) { selectedMovie ->
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.favouritesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = favoritesAdapter
        }
    }
    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return@launch

            val favoriteMovies = database.favouritesDao().getAll(userId)

            withContext(Dispatchers.Main) {
                Log.d("FavouritesFragment", "Favorite Movies Size: ${favoriteMovies.size}")
                if (favoriteMovies.isEmpty()) {
                    Log.d("FavouritesFragment", "Empty List: Showing TextView")
                    binding.emptyFavouritesTextView.visibility = View.VISIBLE
                    binding.favouritesRecyclerView.visibility = View.GONE
                    binding.fabViewAll.visibility = View.VISIBLE
                } else {
                    Log.d("FavouritesFragment", "Non-Empty List: Showing RecyclerView")
                    binding.emptyFavouritesTextView.visibility = View.GONE
                    binding.favouritesRecyclerView.visibility = View.VISIBLE
                    binding.fabViewAll.visibility = View.GONE
                    favoritesAdapter.updateData(favoriteMovies)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
