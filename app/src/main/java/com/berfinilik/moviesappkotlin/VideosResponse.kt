package com.berfinilik.moviesappkotlin

data class VideosResponse(
    val id: Int,
    val results: List<Result>
)