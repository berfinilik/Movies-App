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

    private var currentPage = 1
    var isLoading = false
    private var totalPages = Int.MAX_VALUE

    val apiKey = BuildConfig.TMDB_API_KEY

    fun fetchPopularMovies(language: String = "tr-TR", region: String? = null) {
        if (isLoading || currentPage > totalPages) return
        isLoading = true
        viewModelScope.launch {
            val response = repository.getPopularMovies(apiKey, language, currentPage, region)
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                val currentResponse = _popularMoviesLiveData.value
                val updatedMovies = (currentResponse?.results ?: emptyList()) + movies

                totalPages = response.body()?.total_pages ?: Int.MAX_VALUE
                _popularMoviesLiveData.postValue(
                    PopularMoviesResponse(
                        page = currentPage,
                        results = updatedMovies,
                        total_pages = totalPages,
                        total_results = response.body()?.total_results ?: 0
                    )
                )
                currentPage++
            }
            isLoading = false
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
    fun fetchMoviesByCategory(categoryId: Int, language: String = "tr-TR", region: String? = null) {
        if (isLoading || currentPage > totalPages) return
        isLoading = true

        viewModelScope.launch {
            val response = repository.getMoviesByCategory(apiKey, categoryId, language, currentPage)
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                val currentResponse = _popularMoviesLiveData.value
                val updatedMovies = (currentResponse?.results ?: emptyList()) + movies

                totalPages = response.body()?.total_pages ?: Int.MAX_VALUE
                val updatedResponse = currentResponse?.copy(results = updatedMovies)
                    ?: PopularMoviesResponse(
                        page = currentPage,
                        results = updatedMovies,
                        total_pages = totalPages,
                        total_results = response.body()?.total_results ?: updatedMovies.size
                    )

                _popularMoviesLiveData.postValue(updatedResponse)
                currentPage++
            } else {
                Log.e("MovieViewModel", "Kategoriye göre filmler alınamadı: ${response.errorBody()?.string()}")
            }
            isLoading = false
        }
    }
    fun resetPagination() {
        currentPage = 1
        isLoading = false
        totalPages = Int.MAX_VALUE
    }


}
