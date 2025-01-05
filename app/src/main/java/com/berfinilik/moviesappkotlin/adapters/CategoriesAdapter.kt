package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.Genre
import com.berfinilik.moviesappkotlin.databinding.ItemCategoryBinding

class CategoriesAdapter(
    private var categoriesList: List<Genre>,
    private val onCategoryClick: (Genre) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Genre) {
            binding.categoryNameTextView.text = category.name
            binding.root.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoriesList[position])
    }

    override fun getItemCount() = categoriesList.size

    fun updateData(newCategories: List<Genre>) {
        categoriesList = newCategories
        notifyDataSetChanged()
    }
}
