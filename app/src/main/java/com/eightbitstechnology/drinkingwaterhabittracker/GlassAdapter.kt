package com.eightbitstechnology.drinkingwaterhabittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eightbitstechnology.drinkingwaterhabittracker.databinding.ItemGlassBinding

class GlassAdapter(private val onCheckedChange: (Int, Boolean) -> Unit) :
    ListAdapter<Glass, GlassAdapter.GlassViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlassViewHolder {
        val binding = ItemGlassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GlassViewHolder(binding, onCheckedChange)
    }


    override fun onBindViewHolder(holder: GlassViewHolder, position: Int) {
        val currentGlass = getItem(position)
        holder.bind(currentGlass)
    }

    class GlassViewHolder(
        private val binding: ItemGlassBinding,
        private val onCheckedChange: (Int, Boolean) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(glass: Glass) {
            val context = binding.root.context
            binding.apply {
                completeGlass.isChecked = glass.isDrunk
                completeGlass.text = "Glass ${position + 1}"

                completeGlass.setOnCheckedChangeListener(null) // Clear listener before setting
                completeGlass.setOnCheckedChangeListener { _, isChecked ->
                    onCheckedChange(position, isChecked)
                }
            }
        }

    }

    object DiffCallback : DiffUtil.ItemCallback<Glass>() {
        override fun areItemsTheSame(oldItem: Glass, newItem: Glass): Boolean = oldItem === newItem

        override fun areContentsTheSame(oldItem: Glass, newItem: Glass): Boolean =
            oldItem.id == newItem.id
    }
}