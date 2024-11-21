package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.databinding.ItemSavedMovieBinding
import com.bumptech.glide.Glide
import com.berfinilik.moviesappkotlin.data.model.SavedMovie

class SavedMoviesAdapter(
    private var movies: List<SavedMovie>,
    private val onMovieClick: (Int) -> Unit
) : RecyclerView.Adapter<SavedMoviesAdapter.SavedMovieViewHolder>() {

    inner class SavedMovieViewHolder(private val binding: ItemSavedMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: SavedMovie) {
            binding.movieTitleTextView.text = movie.title
            binding.releaseYearTextView.text = "${movie.releaseYear}"
            Glide.with(binding.moviePosterImageView.context)
                .load(movie.posterUrl)
                .into(binding.moviePosterImageView)

            binding.root.setOnClickListener {
                onMovieClick(movie.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedMovieViewHolder {
        val binding =
            ItemSavedMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedMovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<SavedMovie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
