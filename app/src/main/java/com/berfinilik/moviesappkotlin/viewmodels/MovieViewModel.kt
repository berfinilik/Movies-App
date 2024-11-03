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
import com.berfinilik.moviesappkotlin.data.model.Result

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _popularMoviesLiveData = MutableLiveData<PopularMoviesResponse>()
    val popularMoviesLiveData: LiveData<PopularMoviesResponse> = _popularMoviesLiveData

    private val _genresLiveData = MutableLiveData<GenresResponse>()
    val genresLiveData: LiveData<GenresResponse> = _genresLiveData

    private val _searchResultsLiveData = MutableLiveData<List<Result>>()
    val searchResultsLiveData: LiveData<List<Result>> = _searchResultsLiveData

    val apiKey = BuildConfig.TMDB_API_KEY

    fun fetchPopularMovies(language: String = "tr-TR", page: Int = 1, region: String? = null) {
        viewModelScope.launch {
            val response = repository.getPopularMovies(apiKey, language, page, region)
            if (response.isSuccessful) {
                Log.d("MovieViewModel", "Popüler filmler başarıyla alındı: ${response.body()?.results?.size} items")
                _popularMoviesLiveData.postValue(response.body())
            } else {
                Log.e("MovieViewModel", "Popüler filmler alınamadı: ${response.errorBody()?.string()}")
            }
        }
    }

    fun fetchMovieGenres(language: String = "tr-TR") {
        viewModelScope.launch {
            val response = repository.getMovieGenres(apiKey, language)
            if (response.isSuccessful) {
                Log.d("MovieViewModel", "Kategoriler başarıyla alındı: ${response.body()?.genres?.size} items")
                _genresLiveData.postValue(response.body())
            } else {
                Log.e("MovieViewModel", "Kategoriler alınamadı: ${response.errorBody()?.string()}")
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            val response = repository.searchMovies(apiKey, query)
            if (response.isSuccessful) {
                _searchResultsLiveData.postValue(response.body()?.results)
            } else {
                Log.e("MovieViewModel", "Arama başarısız: ${response.errorBody()?.string()}")
            }
        }
    }
    fun fetchMoviesByCategory(categoryId: Int, language: String = "tr-TR", page: Int = 1) {
        viewModelScope.launch {
            val response = repository.getMoviesByCategory(apiKey, categoryId, language, page)
            if (response.isSuccessful) {
                _popularMoviesLiveData.postValue(response.body())
            } else {
                Log.e("MovieViewModel", "Kategoriye göre filmler alınamadı: ${response.errorBody()?.string()}")
            }
        }
    }
}
