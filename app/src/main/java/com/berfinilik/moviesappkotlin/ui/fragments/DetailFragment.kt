package com.berfinilik.moviesappkotlin.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berfinilik.moviesappkotlin.BuildConfig
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.data.model.MovieDetailsResponse
import com.berfinilik.moviesappkotlin.adapters.CastAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.database.AppDatabase
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie
import com.berfinilik.moviesappkotlin.data.model.SavedMovie
import com.berfinilik.moviesappkotlin.databinding.FragmentDetailBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private var movieId: Int? = null
    private var isFavorite = false

    private var movie: MovieDetailsResponse? = null
    private var videoKey: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            movieId = it.getInt("movieId")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieId?.let { id ->
            fetchMovieDetails(id)
            checkIfFavorite(id)
            fetchMovieVideos(id)
        }

        binding.backImageView.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.favImageView.setOnClickListener {
            movie?.let { movieDetails ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(requireContext())
                    val isAlreadyFavorite = database.favouritesDao().isMovieFavorite(movieDetails.id)

                    withContext(Dispatchers.Main) {
                        if (!isAlreadyFavorite) {
                            addMovieToFavorites(movieDetails)
                            binding.favImageView.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                            showSnackbar("Favorilere eklendi.")
                            isFavorite = true
                        } else {
                            removeMovieFromFavorites(movieDetails.id)
                            binding.favImageView.clearColorFilter()
                            showSnackbar("Favorilerden kaldırıldı.")
                            isFavorite = false
                        }
                    }
                }
            }
        }
        binding.addToSavedBtn.setOnClickListener {
            movie?.let { movieDetails ->
                addMovieToSavedList(movieDetails)
                showSnackbar("${movieDetails.title} kaydedilenler listesine eklendi.")
            }
        }
        binding.watchTrailerBtn.setOnClickListener {
            videoKey?.let { key ->
                val action = DetailFragmentDirections.actionDetailFragmentToYouTubePlayerFragment(key)
                findNavController().navigate(action)
            }
        }


    }
    private fun fetchMovieVideos(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = ApiClient.apiService.getMovieVideos(
                movieId = movieId,
                apiKey = BuildConfig.TMDB_API_KEY
            )
            if (response.isSuccessful && response.body() != null) {
                val videos = response.body()?.results?.filter { it.site == "YouTube" }
                if (!videos.isNullOrEmpty()) {
                    videoKey = videos.first().key
                    binding.watchTrailerBtn.visibility = View.VISIBLE
                } else {
                    binding.watchTrailerBtn.visibility = View.GONE
                }
            } else {
                binding.watchTrailerBtn.visibility = View.GONE
            }
        }
    }


    private fun addMovieToSavedList(movie: MovieDetailsResponse) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val savedMovie = SavedMovie(
                id = movie.id,
                title = movie.title,
                releaseYear = movie.release_date?.split("-")?.get(0)?.toIntOrNull() ?: 0,
                posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
            )
            database.savedMoviesDao().insertSavedMovie(savedMovie)
        }
    }
    private fun checkIfFavorite(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            val favoriteMovie = database.favouritesDao().getMovieById(movieId)
            withContext(Dispatchers.Main) {
                if (favoriteMovie != null) {
                    isFavorite = true
                    binding.favImageView.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.red))
                } else {
                    isFavorite = false
                    binding.favImageView.clearColorFilter()
                }
            }
        }
    }



    private fun removeMovieFromFavorites(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            database.favouritesDao().deleteById(movieId)
            withContext(Dispatchers.Main) {
                showSnackbar("Film favorilerden kaldırıldı.")
            }
        }
    }



    private fun addMovieToFavorites(movie: MovieDetailsResponse) {
        val releaseYear = movie.release_date ?.split("-")?.get(0)?.toIntOrNull() ?: 0

        val favoriteMovie = FavouriteMovie(
            id = movie.id,
            title = movie.title,
            releaseYear = releaseYear,
            posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(requireContext())
            database.favouritesDao().insertAll(favoriteMovie)
            withContext(Dispatchers.Main) {
                showSnackbar("${movie.title} favorilere eklendi.")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun fetchMovieDetails(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = ApiClient.apiService.getMovieDetails(
                movieId = movieId,
                apiKey = BuildConfig.TMDB_API_KEY
            )
            if (response.isSuccessful && response.body() != null) {
                val movie = response.body()
                this@DetailFragment.movie = movie
                Log.d("DetailFragment", "Movie Details: $movie")

                binding.titleTextView.text = movie?.title
                binding.movieSummaryTextView.text = movie?.overview

                val rating = movie?.vote_average?.let { "%.1f".format(it) } ?: "Bilinmiyor"
                binding.ratingTextView.text = "$rating / 10"

                val releaseYear = movie?.release_date?.split("-")?.get(0) ?: "Bilinmiyor"
                binding.movieReleaseYearTextView.text = "$releaseYear"
                val duration = movie?.runtime?.let { "$it dk" } ?: "Bilinmiyor"
                binding.movieDurationTextView.text = "$duration"
                val genres = movie?.genres?.joinToString(", ") { it.name } ?: "Bilinmiyor"
                binding.movieCategoryTextView.text = "$genres"


                movie?.poster_path?.let { posterPath ->
                    Glide.with(this@DetailFragment)
                        .load("https://image.tmdb.org/t/p/w500$posterPath")
                        .into(binding.moviesImageView)
                } ?: run {
                    Log.e("DetailFragment", "Poster yolu boş.")
                }

                movie?.credits?.cast?.let { castList ->
                    if (castList.isNotEmpty()) {
                        val castAdapter = CastAdapter(castList)
                        binding.castRecyclerView.layoutManager = GridLayoutManager(
                            requireContext(),
                            2,
                            GridLayoutManager.VERTICAL,
                            false
                        )
                        binding.castRecyclerView.adapter = castAdapter
                    } else {
                        Log.e("DetailFragment", "Cast bilgisi mevcut değil.")
                    }
                }
            } else {
                Log.e("DetailFragment", "Film ayrıntıları getirilemedi veya yanıt boş.")
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}