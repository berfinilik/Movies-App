package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.Cast
import com.berfinilik.moviesappkotlin.R
import com.bumptech.glide.Glide

class CastAdapter(private val castList: List<Cast>) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val castNameTextView: TextView = itemView.findViewById(R.id.castNameTextView)
        private val castCharacterTextView: TextView = itemView.findViewById(R.id.characterNameTextView)
        private val castImageView: ImageView = itemView.findViewById(R.id.castProfileImageView)

        fun bind(cast: Cast) {
            if (cast.profile_path != null) {
                castImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load("https://image.tmdb.org/t/p/w500${cast.profile_path}")
                    .into(castImageView)
            } else {
                castImageView.visibility = View.GONE
            }
            castNameTextView.text = cast.name
            castCharacterTextView.text = cast.character
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(castList[position])
    }

    override fun getItemCount(): Int = castList.size
}
