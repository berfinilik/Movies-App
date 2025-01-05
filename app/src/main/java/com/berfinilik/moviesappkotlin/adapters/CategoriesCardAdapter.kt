package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.Movie
import com.berfinilik.moviesappkotlin.databinding.ItemCategoryCardBinding
import com.bumptech.glide.Glide

class CategoriesCardAdapter(
    private var movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<CategoriesCardAdapter.CategoryMovieViewHolder>() {

    inner class CategoryMovieViewHolder(private val binding: ItemCategoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            with(binding) {
                movieTitleTextView.text = movie.title
                movieReleaseDateTextView.text = movie.releaseYear.toString()

                Glide.with(root.context)
                    .load(movie.posterUrl)
                    .into(moviePosterImageView)

                root.setOnClickListener {
                    onMovieClick(movie)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryMovieViewHolder {
        val binding = ItemCategoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryMovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size

    fun updateData(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
