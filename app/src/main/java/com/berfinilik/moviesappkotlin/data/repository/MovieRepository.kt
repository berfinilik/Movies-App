package com.berfinilik.moviesappkotlin.data.repository

import android.content.Context
import com.berfinilik.moviesappkotlin.api.MovieApiService
import com.berfinilik.moviesappkotlin.data.model.GenresResponse
import com.berfinilik.moviesappkotlin.data.model.PopularMoviesResponse
import com.berfinilik.moviesappkotlin.utils.KeyStore
import retrofit2.Response

class MovieRepository(
    private val apiService: MovieApiService,
    private val context: Context
) {
    private val keyStore = KeyStore(context)

    private val apiKey: String by lazy {
        val sharedPreferences = context.getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE)
        val encryptedApiKey = sharedPreferences.getString("encryptedApiKey", null)
        val iv = sharedPreferences.getString("encryptionIv", null)

        if (encryptedApiKey != null && iv != null) {
            keyStore.decryptData(encryptedApiKey, iv)
        } else {
            throw IllegalStateException("API key not found! Şifrelenmiş API key eksik.")
        }
    }

    suspend fun getPopularMovies(
        page: Int = 1,
        language: String = "tr-TR",
        region: String? = null
    ): Response<PopularMoviesResponse> {
        return apiService.getPopularMovies(apiKey = apiKey, page = page, language = language, region = region)
    }

    suspend fun getMovieGenres(
        language: String = "tr-TR"
    ): Response<GenresResponse> {
        return apiService.getMovieGenres(apiKey, language)
    }

    suspend fun searchMovies(query: String): Response<PopularMoviesResponse> {
        return apiService.searchMovies(apiKey, query)
    }
    suspend fun getMoviesByCategory(
        categoryId: Int,
        language: String = "tr-TR",
        page: Int = 1
    ): Response<PopularMoviesResponse> {
        return apiService.getMoviesByCategory(apiKey, categoryId, language, page)
    }
}
