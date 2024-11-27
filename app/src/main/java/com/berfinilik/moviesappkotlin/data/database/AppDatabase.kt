package com.berfinilik.moviesappkotlin.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.berfinilik.moviesappkotlin.data.model.SavedMovie
import com.berfinilik.moviesappkotlin.data.dao.SavedMoviesDao
import com.berfinilik.moviesappkotlin.data.dao.FavouritesDao
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie

@Database(entities = [FavouriteMovie::class, SavedMovie::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouritesDao(): FavouritesDao
    abstract fun savedMoviesDao(): SavedMoviesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movie-database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_movies` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `title` TEXT NOT NULL,
                        `releaseYear` INTEGER NOT NULL,
                        `posterUrl` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `saved_movies`")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_movies` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `title` TEXT NOT NULL,
                        `releaseYear` INTEGER NOT NULL,
                        `posterUrl` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE favorite_movies ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}