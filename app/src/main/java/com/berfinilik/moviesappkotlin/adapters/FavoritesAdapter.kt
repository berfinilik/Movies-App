package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie
import com.berfinilik.moviesappkotlin.databinding.ItemFavoriteMovieBinding
import com.bumptech.glide.Glide

class FavoritesAdapter(
    private var moviesList: List<FavouriteMovie>,
    private val onClick: (FavouriteMovie) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(private val binding: ItemFavoriteMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: FavouriteMovie) {
            binding.favoriteMovieTitle.text = movie.title
            binding.favoriteMovieReleaseDateTextView.text = movie.releaseYear.toString()

            Glide.with(binding.root.context)
                .load(movie.posterUrl)
                .into(binding.favoriteMoviePoster)

            binding.root.setOnClickListener {
                onClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(moviesList[position])
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newFavorites: List<FavouriteMovie>) {
        moviesList = newFavorites
        notifyDataSetChanged()
    }
}
