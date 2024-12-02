package com.berfinilik.moviesappkotlin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import com.berfinilik.moviesappkotlin.data.model.Result
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import kotlinx.coroutines.launch

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

    fun fetchPopularMovies(language: String = "tr-TR", region: String? = null) {
        if (isLoading || currentPage > totalPages) return
        isLoading = true
        viewModelScope.launch {
            val response = repository.getPopularMovies(language, currentPage, region)
            if (response.isSuccessful) {
                val movies = response.body()?.results.orEmpty()
                val currentResponse = _popularMoviesLiveData.value
                val updatedMovies = (currentResponse?.results.orEmpty()) + movies

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
            val response = repository.getMovieGenres(language)
            if (response.isSuccessful) {
                _genresLiveData.postValue(response.body())
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            val response = repository.searchMovies(query)
            if (response.isSuccessful) {
                _searchResultsLiveData.postValue(response.body()?.results.orEmpty())
            }
        }
    }
    fun fetchMoviesByCategory(categoryId: Int, language: String = "tr-TR", region: String? = null) {
        if (isLoading || currentPage > totalPages) return
        isLoading = true
        viewModelScope.launch {
            val response = repository.getMoviesByCategory(categoryId, language, currentPage)
            if (response.isSuccessful) {
                val movies = response.body()?.results.orEmpty()
                val currentResponse = _popularMoviesLiveData.value
                val updatedMovies = (currentResponse?.results.orEmpty()) + movies

                totalPages = response.body()?.total_pages ?: Int.MAX_VALUE
                _popularMoviesLiveData.postValue(
                    currentResponse?.copy(results = updatedMovies)
                        ?: PopularMoviesResponse(
                            page = currentPage,
                            results = updatedMovies,
                            total_pages = totalPages,
                            total_results = response.body()?.total_results ?: updatedMovies.size
                        )
                )
                currentPage++
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
