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
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentAllPopularMoviesBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel

class AllPopularMoviesFragment : Fragment() {

    private var _binding: FragmentAllPopularMoviesBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { MovieRepository(ApiClient.apiService, requireContext()) }
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(repository)
    }

    private lateinit var popularMoviesAdapter: PopularMoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPopularMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        movieViewModel.fetchPopularMovies()
    }

    private fun setupRecyclerView() {
        popularMoviesAdapter = PopularMoviesAdapter(emptyList()) { selectedMovie ->
            val action = AllPopularMoviesFragmentDirections.actionAllPopularMoviesFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.recyclerViewAllPopular.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = popularMoviesAdapter
        }
    }

    private fun observeViewModel() {
        movieViewModel.popularMoviesLiveData.observe(viewLifecycleOwner) { popularMoviesResponse ->
            popularMoviesResponse?.let {
                if (it.results.isNotEmpty()) {
                    binding.recyclerViewAllPopular.post {
                        popularMoviesAdapter.updateData(it.results)
                    }
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
