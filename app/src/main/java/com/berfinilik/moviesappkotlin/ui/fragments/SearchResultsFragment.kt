package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.adapters.SearchMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentSearchResultsBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.android.material.snackbar.Snackbar

class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { MovieRepository(ApiClient.apiService) }
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(repository)
    }

    private lateinit var searchMoviesAdapter: SearchMoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val query = arguments?.getString("query")
        query?.let {
            movieViewModel.searchMovies(it)
            binding.searchResultsTitle.text = "'$it' İçin Sonuçlar"
        }
    }

    private fun setupRecyclerView() {
        searchMoviesAdapter = SearchMoviesAdapter(emptyList()) { selectedMovie ->
            val action = SearchResultsFragmentDirections.actionSearchResultsFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewSearchResults.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = searchMoviesAdapter
        }
    }

    private fun observeViewModel() {
        movieViewModel.searchResultsLiveData.observe(viewLifecycleOwner) { searchResults ->
            searchResults?.let {
                if (it.isNotEmpty()) {
                    searchMoviesAdapter.updateData(it)
                } else {
                    showSnackbar("Sonuç bulunamadı.")
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
