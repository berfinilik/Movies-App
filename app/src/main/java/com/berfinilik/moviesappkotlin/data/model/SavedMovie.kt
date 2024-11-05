package com.berfinilik.moviesappkotlin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_movies")
data class SavedMovie(
    @PrimaryKey val id: Int,
    val title: String,
    val releaseYear: Int,
    val posterUrl: String
)

