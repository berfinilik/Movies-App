package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.MovieResult
import com.berfinilik.moviesappkotlin.databinding.ItemRandomMovieBinding
import com.bumptech.glide.Glide

class RandomMovieAdapter(
    private val movies: List<MovieResult>,
    private val onMovieClick: (MovieResult) -> Unit
) : RecyclerView.Adapter<RandomMovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(private val binding: ItemRandomMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieResult) {
            Glide.with(binding.root)
                .load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                .into(binding.imageViewRandomMovie)

            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemRandomMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}
