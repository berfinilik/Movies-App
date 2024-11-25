package com.berfinilik.moviesappkotlin.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.berfinilik.moviesappkotlin.data.model.SavedMovie

@Dao
interface SavedMoviesDao {
    @Query("SELECT * FROM saved_movies")
    fun getAllSavedMovies(): List<SavedMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMovie(movie: SavedMovie)

    @Query("DELETE FROM saved_movies WHERE id = :movieId")
    suspend fun deleteSavedMovieById(movieId: Int)
}
