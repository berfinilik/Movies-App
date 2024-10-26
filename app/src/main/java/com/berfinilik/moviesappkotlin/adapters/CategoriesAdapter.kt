package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.data.model.Genre

class CategoriesAdapter(private var categoriesList: List<Genre>) :
    RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: TextView = view.findViewById(R.id.categoryNameTextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_category, viewGroup, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: CategoryViewHolder, position: Int) {
        viewHolder.categoryTextView.text = categoriesList[position].name
    }

    override fun getItemCount() = categoriesList.size

    fun updateData(newCategories: List<Genre>) {
        categoriesList = newCategories
        notifyDataSetChanged()
    }
}
