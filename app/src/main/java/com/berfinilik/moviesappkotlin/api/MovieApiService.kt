package com.berfinilik.moviesappkotlin.api

import com.berfinilik.moviesappkotlin.model.GenresResponse
import com.berfinilik.moviesappkotlin.model.PopularMoviesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("language") language:String= "tr-TR",
        @Query("page") page:Int=1,
        @Query("region") region: String? = null

    ): Call<PopularMoviesResponse>
    @GET("genre/movie/list")
    fun getMovieGenres(
        @Query("language") language: String="tr-TR"
    ):Call<GenresResponse>
}