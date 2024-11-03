package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentCategoryMoviesBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.android.material.snackbar.Snackbar


class CategoryMoviesFragment : Fragment() {

    private var _binding: FragmentCategoryMoviesBinding? = null
    private val binding get() = _binding!!
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(MovieRepository(ApiClient.apiService))
    }

    private lateinit var moviesAdapter: PopularMoviesAdapter
    private var categoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryId = arguments?.getInt("categoryId")

        moviesAdapter = PopularMoviesAdapter(emptyList()) { selectedMovie ->
            val action = CategoryMoviesFragmentDirections.actionCategoryMoviesFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = moviesAdapter
        }
        observeViewModel()
        categoryId?.let { id ->
            movieViewModel.fetchMoviesByCategory(id)
        }
    }

    private fun observeViewModel() {
        movieViewModel.popularMoviesLiveData.observe(viewLifecycleOwner) { response ->
            response?.let {
                if (it.results.isNotEmpty()) {
                    moviesAdapter.updateData(it.results)
                } else {
                    showSnackbar("Bu kategoride film bulunamadı.")
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
