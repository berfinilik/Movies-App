package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.data.model.FavouriteMovie
import com.bumptech.glide.Glide

class FavoritesAdapter(
    private var moviesList: List<FavouriteMovie>,
    private val onClick: (FavouriteMovie) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.favoriteMovieTitle)
        val posterImageView: ImageView = view.findViewById(R.id.favoriteMoviePoster)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_favorite_movie, viewGroup, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: FavoriteViewHolder, position: Int) {
        val movie = moviesList[position]
        viewHolder.titleTextView.text = movie.title

        Glide.with(viewHolder.itemView.context)
            .load(movie.posterUrl)
            .into(viewHolder.posterImageView)
        viewHolder.itemView.setOnClickListener {
            onClick(movie)
        }
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newFavorites: List<FavouriteMovie>) {
        moviesList = newFavorites
        notifyDataSetChanged()
    }
}