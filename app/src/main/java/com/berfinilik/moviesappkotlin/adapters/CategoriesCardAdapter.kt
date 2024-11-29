package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.data.model.Movie
import com.bumptech.glide.Glide

class CategoriesCardAdapter(
    private var movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<CategoriesCardAdapter.CategoryMovieViewHolder>() {

    class CategoryMovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterImageView: ImageView = view.findViewById(R.id.moviePosterImageView)
        val titleTextView: TextView = view.findViewById(R.id.movieTitleTextView)
        val releaseDateTextView: TextView = view.findViewById(R.id.movieReleaseDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryMovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleTextView.text = movie.title
        holder.releaseDateTextView.text = "Yayın Tarihi: ${movie.releaseYear}"
        Glide.with(holder.itemView.context)
            .load(movie.posterUrl)
            .into(holder.posterImageView)

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }

    override fun getItemCount() = movies.size

    fun updateData(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
