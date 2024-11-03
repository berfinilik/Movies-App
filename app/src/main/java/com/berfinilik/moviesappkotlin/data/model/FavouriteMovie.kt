package com.berfinilik.moviesappkotlin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavouriteMovie(
    @PrimaryKey val id: Int,
    val title: String,
    val releaseYear: Int,
    val posterUrl: String
)
