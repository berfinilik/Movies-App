package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentCategoryMoviesBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel


class CategoryMoviesFragment : Fragment() {

    private var _binding: FragmentCategoryMoviesBinding? = null
    private val binding get() = _binding!!
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(MovieRepository(ApiClient.apiService))
    }

    private lateinit var moviesAdapter: PopularMoviesAdapter
    private var categoryId: Int? = null
    private var categoryName: String? = null


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
        categoryName = arguments?.getString("categoryName")

        binding.categoryTitleTextView.text = categoryName ?: "Kategori"


        moviesAdapter = PopularMoviesAdapter(emptyList()) { selectedMovie ->
            val action = CategoryMoviesFragmentDirections.actionCategoryMoviesFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = moviesAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (!movieViewModel.isLoading && lastVisibleItemPosition + 5 >= totalItemCount) {
                        categoryId?.let { id ->
                            movieViewModel.fetchMoviesByCategory(id)
                        }
                    }
                }
            })
        }
        observeViewModel()
        categoryId?.let { id ->
            movieViewModel.resetPagination()
            movieViewModel.fetchMoviesByCategory(id)
        }
    }

    private fun observeViewModel() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyMessageTextView.visibility = View.GONE
        binding.moviesRecyclerView.visibility = View.GONE

        movieViewModel.popularMoviesLiveData.observe(viewLifecycleOwner) { response ->
            binding.progressBar.visibility = View.GONE
            response?.let {
                if (it.results.isNotEmpty()) {
                    binding.moviesRecyclerView.visibility = View.VISIBLE
                    binding.emptyMessageTextView.visibility = View.GONE
                    moviesAdapter.updateData(it.results)
                } else {
                    binding.moviesRecyclerView.visibility = View.GONE
                    binding.emptyMessageTextView.visibility = View.VISIBLE
                }
            } ?: run {
                binding.moviesRecyclerView.visibility = View.GONE
                binding.emptyMessageTextView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
