package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.adapters.CategoriesAdapter
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentHomeBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { MovieRepository(ApiClient.apiService) }
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(repository)
    }

    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var popularMoviesAdapter: PopularMoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = arguments?.getString("userName") ?: "Anonim Kullanıcı"
        binding.textViewUserName.text = "Hoşgeldiniz, $userName"


        setupRecyclerViews()
        observeViewModel()
        movieViewModel.fetchMovieGenres()
        movieViewModel.fetchPopularMovies()
    }

    private fun setupRecyclerViews() {
        categoriesAdapter = CategoriesAdapter(emptyList())
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoriesAdapter
        }

        popularMoviesAdapter = PopularMoviesAdapter(emptyList()) { selectedMovie ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.popularsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = popularMoviesAdapter
        }
    }

    private fun observeViewModel() {
        movieViewModel.genresLiveData.observe(viewLifecycleOwner) { genresResponse ->
            genresResponse?.let {
                if (it.genres.isNotEmpty()) {
                    binding.categoriesRecyclerView.post {
                        categoriesAdapter.updateData(it.genres)
                    }
                } else {
                    showSnackbar("Kategoriler yüklenemedi")
                }
            }
        }

        movieViewModel.popularMoviesLiveData.observe(viewLifecycleOwner) { popularMoviesResponse ->
            popularMoviesResponse?.let {
                if (it.results.isNotEmpty()) {
                    binding.popularsRecyclerView.post {
                        popularMoviesAdapter.updateData(it.results)
                    }
                } else {
                    showSnackbar("Popüler filmler yüklenemedi")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
