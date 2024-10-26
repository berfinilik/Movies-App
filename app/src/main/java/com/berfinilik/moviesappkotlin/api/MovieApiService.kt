package com.berfinilik.moviesappkotlin.api

import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "tr-TR",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): Response<PopularMoviesResponse>

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "tr-TR"
    ): Response<GenresResponse>
}