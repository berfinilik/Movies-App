package com.berfinilik.moviesappkotlin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SavedMoviesDao {
    @Query("SELECT * FROM saved_movies")
    fun getAllSavedMovies(): List<SavedMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(movie: SavedMovie)

    @Query("DELETE FROM saved_movies WHERE id = :movieId")
    fun deleteById(movieId: Int)
}
