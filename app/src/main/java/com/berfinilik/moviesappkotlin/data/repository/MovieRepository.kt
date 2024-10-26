package com.berfinilik.moviesappkotlin.data.repository

import com.berfinilik.moviesappkotlin.api.MovieApiService
import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import retrofit2.Response

class MovieRepository(private val apiService: MovieApiService) {

    suspend fun getPopularMovies(
        apiKey: String,
        language: String = "tr-TR",
        page: Int = 1,
        region: String? = null
    ): Response<PopularMoviesResponse> {
        return apiService.getPopularMovies(apiKey, language, page, region)
    }

    suspend fun getMovieGenres(
        apiKey: String,
        language: String = "tr-TR"
    ): Response<GenresResponse> {
        return apiService.getMovieGenres(apiKey, language)
    }
}