package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.MovieResult
import com.berfinilik.moviesappkotlin.databinding.ItemPopularMovieBinding
import com.bumptech.glide.Glide

class PopularMoviesAdapter(
    private var moviesList: List<MovieResult>,
    private val onClick: (MovieResult) -> Unit
) : RecyclerView.Adapter<PopularMoviesAdapter.PopularViewHolder>() {

    inner class PopularViewHolder(private val binding: ItemPopularMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieResult) {
            with(binding) {
                nameFilmTextView.text = movie.title

                Glide.with(root.context)
                    .load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                    .into(filmImageView)

                root.setOnClickListener {
                    onClick(movie)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = ItemPopularMovieBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: PopularViewHolder, position: Int) {
        viewHolder.bind(moviesList[position])
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newMovies: List<MovieResult>) {
        moviesList = newMovies
        notifyDataSetChanged()
    }
}
