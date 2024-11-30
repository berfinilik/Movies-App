package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.data.model.Result
import com.bumptech.glide.Glide

class SearchMoviesAdapter(
    private var moviesList: List<Result>,
    private val onClick: (Result) -> Unit
) : RecyclerView.Adapter<SearchMoviesAdapter.SearchViewHolder>() {

    class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterImageView: ImageView = view.findViewById(R.id.searchMoviePosterImageView)
        val titleTextView: TextView = view.findViewById(R.id.searchMovieTitleTextView)
        val releaseYearTextView: TextView = view.findViewById(R.id.searchMovieReleaseDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val movie = moviesList[position]
        holder.titleTextView.text = movie.title
        holder.releaseYearTextView.text = "Yıl: ${movie.release_date}"

        Glide.with(holder.itemView.context)
            .load("https://image.tmdb.org/t/p/w500" + movie.poster_path)
            .into(holder.posterImageView)

        holder.itemView.setOnClickListener { onClick(movie) }
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newMovies: List<Result>) {
        moviesList = newMovies
        notifyDataSetChanged()
    }
}
