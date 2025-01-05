package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.Cast
import com.berfinilik.moviesappkotlin.databinding.ItemCastBinding
import com.bumptech.glide.Glide

class CastAdapter(private val castList: List<Cast>) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(private val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cast: Cast) {
            with(binding) {
                if (cast.profile_path != null) {
                    castProfileImageView.visibility = android.view.View.VISIBLE
                    Glide.with(root.context)
                        .load("https://image.tmdb.org/t/p/w500${cast.profile_path}")
                        .into(castProfileImageView)
                } else {
                    castProfileImageView.visibility = android.view.View.GONE
                }

                castNameTextView.text = cast.name
                characterNameTextView.text = cast.character
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(castList[position])
    }

    override fun getItemCount(): Int = castList.size
}
