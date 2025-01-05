package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.MovieResult
import com.berfinilik.moviesappkotlin.databinding.ItemSearchResultBinding
import com.bumptech.glide.Glide

class SearchMoviesAdapter(
    private var moviesList: List<MovieResult>,
    private val onClick: (MovieResult) -> Unit
) : RecyclerView.Adapter<SearchMoviesAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieResult) {
            with(binding) {
                searchMovieTitleTextView.text = movie.title
                searchMovieReleaseDateTextView.text = "Yıl: ${movie.release_date}"

                Glide.with(root.context)
                    .load("https://image.tmdb.org/t/p/w500${movie.poster_path}")
                    .into(searchMoviePosterImageView)

                root.setOnClickListener {
                    onClick(movie)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(moviesList[position])
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newMovies: List<MovieResult>) {
        moviesList = newMovies
        notifyDataSetChanged()
    }
}
