package com.berfinilik.moviesappkotlin.api

import com.berfinilik.moviesappkotlin.data.model.VideosResponse
import com.berfinilik.moviesappkotlin.data.model.MovieDetailsResponse
import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.MovieResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "tr-TR",
        @Query("region") region: String? = null
    ): Response<PopularMoviesResponse>
    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "tr-TR"
    ): Response<GenresResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "tr-TR",
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "credits"

        ): Response<MovieDetailsResponse>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "tr-TR",
        @Query("page") page: Int = 1
    ): Response<PopularMoviesResponse>

    @GET("discover/movie")
    suspend fun getMoviesByCategory(
        @Query("api_key") apiKey: String,
        @Query("with_genres") categoryId: Int,
        @Query("language") language: String = "tr-TR",
        @Query("page") page: Int = 1
    ): Response<PopularMoviesResponse>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("language") language:String="tr-TR",
        @Query("api_key") apiKey: String
    ):Response<VideosResponse>

    @GET("discover/movie")
    suspend fun getDiscoverMovies(
        @Query("api_key") apiKey: String,
        @Query("sort_by") sortBy: String
    ): Response<MovieResponse>

}
