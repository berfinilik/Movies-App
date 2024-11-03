package com.berfinilik.moviesappkotlin.data.model


data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String,
    val releaseDate: String,
    val credits: MovieCastAndCrew?
)
