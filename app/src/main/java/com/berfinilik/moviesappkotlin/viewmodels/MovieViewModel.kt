package com.berfinilik.moviesappkotlin.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berfinilik.moviesappkotlin.BuildConfig
import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import kotlinx.coroutines.launch



class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _popularMoviesLiveData = MutableLiveData<PopularMoviesResponse>()
    val popularMoviesLiveData: LiveData<PopularMoviesResponse> = _popularMoviesLiveData

    private val _genresLiveData = MutableLiveData<GenresResponse>()
    val genresLiveData: LiveData<GenresResponse> = _genresLiveData

    val apiKey = BuildConfig.TMDB_API_KEY


    fun fetchPopularMovies(language: String = "tr-TR", page: Int = 1, region: String? = null) {
        viewModelScope.launch {
            val response =
                repository.getPopularMovies(BuildConfig.TMDB_API_KEY, language, page, region)
            if (response.isSuccessful) {
                Log.d("MovieViewModel", "Popular movies fetched successfully: ${response.body()?.results?.size} items")
                _popularMoviesLiveData.postValue(response.body())
            } else {
                Log.e("MovieViewModel", "Failed to fetch popular movies: ${response.errorBody()?.string()}")

            }
        }
    }

    fun fetchMovieGenres(language: String = "tr-TR") {
        viewModelScope.launch {
            val response = repository.getMovieGenres(apiKey, language)
            if (response.isSuccessful) {
                Log.d("MovieViewModel", "Genres fetched successfully: ${response.body()?.genres?.size} items")
                _genresLiveData.postValue(response.body())
            } else {
                Log.e("MovieViewModel", "Failed to fetch genres: ${response.errorBody()?.string()}")
            }
        }
    }
}