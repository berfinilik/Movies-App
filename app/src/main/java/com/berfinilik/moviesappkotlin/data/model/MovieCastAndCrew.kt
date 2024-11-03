package com.berfinilik.moviesappkotlin.data.model


data class MovieCastAndCrew(
    val cast: List<Cast>,
    val crew: List<Crew>,
    val id: Int
)