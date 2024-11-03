package com.berfinilik.moviesappkotlin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favorite_movies")
    fun getAll(): List<FavouriteMovie>

    @Query("SELECT * FROM favorite_movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Int): FavouriteMovie?

    @Insert
    fun insertAll(vararg favouriteMovie: FavouriteMovie)

    @Query("DELETE FROM favorite_movies WHERE id = :movieId")
    fun deleteById(movieId: Int)



}
