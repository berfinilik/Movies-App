package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.MovieResult
import com.berfinilik.moviesappkotlin.databinding.ItemAllPopularMovieBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class AllPopularMoviesAdapter(
    private var movies: List<MovieResult>,
    private val onItemClick: (MovieResult) -> Unit
) : RecyclerView.Adapter<AllPopularMoviesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAllPopularMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieResult) {
            binding.popularMovieTitle.text = movie.title
            binding.popularMovieReleaseDate.text = movie.release_date

            val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"

            Glide.with(binding.popularMoviePoster.context)
                .load(posterUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.popularMoviePoster)

            binding.root.setOnClickListener {
                onItemClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllPopularMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun updateData(newMovies: List<MovieResult>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}