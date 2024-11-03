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

class PopularMoviesAdapter(
    private var moviesList: List<Result>,
    private val onClick: (Result) -> Unit
) : RecyclerView.Adapter<PopularMoviesAdapter.PopularViewHolder>() {

    class PopularViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.nameFilmTextView)
        val posterImageView: ImageView = view.findViewById(R.id.filmImageView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PopularViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_popular_movie, viewGroup, false)
        return PopularViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PopularViewHolder, position: Int) {
        val movie = moviesList[position]
        viewHolder.titleTextView.text = movie.title

        Glide.with(viewHolder.itemView.context)
            .load("https://image.tmdb.org/t/p/w500" + movie.poster_path)
            .into(viewHolder.posterImageView)

        viewHolder.itemView.setOnClickListener { onClick(movie) }
    }

    override fun getItemCount() = moviesList.size

    fun updateData(newMovies: List<Result>) {
        moviesList = newMovies
        notifyDataSetChanged()
    }
}
