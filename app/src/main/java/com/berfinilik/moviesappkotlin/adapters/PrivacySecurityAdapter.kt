package com.berfinilik.moviesappkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.data.model.SettingOption
import com.berfinilik.moviesappkotlin.databinding.ItemPrivacySecurityOptionBinding

class PrivacySecurityAdapter(
    private val items: List<SettingOption>,
    private val onItemClick: (SettingOption) -> Unit
) : RecyclerView.Adapter<PrivacySecurityAdapter.PrivacySecurityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacySecurityViewHolder {
        val binding = ItemPrivacySecurityOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrivacySecurityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrivacySecurityViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class PrivacySecurityViewHolder(private val binding: ItemPrivacySecurityOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingOption) {
            binding.icon.setImageResource(item.icon)
            binding.title.text = item.title

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
