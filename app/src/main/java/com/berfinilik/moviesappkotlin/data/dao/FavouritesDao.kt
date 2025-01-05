package com.berfinilik.moviesappkotlin.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie

@Dao
interface FavouritesDao {

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :movieId AND userId = :userId)")
    suspend fun isMovieFavorite(movieId: Int, userId: String): Boolean

    @Query("SELECT * FROM favorite_movies WHERE userId = :userId")
    suspend fun getAll(userId: String): List<FavouriteMovie>

    @Query("SELECT * FROM favorite_movies WHERE id = :movieId AND userId = :userId LIMIT 1")
    suspend fun getMovieById(movieId: Int, userId: String): FavouriteMovie?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg favouriteMovie: FavouriteMovie)

    @Query("DELETE FROM favorite_movies WHERE id = :movieId AND userId = :userId")
    suspend fun deleteById(movieId: Int, userId: String)
}

