package com.berfinilik.moviesappkotlin.data.model


data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String,
    val release_date : String,
    val runtime: Int?,
    val genres: List<Genre>?,
    val vote_average: Double?,
    val credits: MovieCastAndCrew?
)
