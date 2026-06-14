package com.budgetawesome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetawesome.data.entity.Badge
import com.budgetawesome.databinding.ItemBadgeBinding

/**
 * BadgeAdapter - RecyclerView adapter for displaying earned badges in a grid.
 * Each badge card shows the icon, title, description and date earned.
 * Author: Rofhiwa Siaga
 */
class BadgeAdapter : ListAdapter<Badge, BadgeAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(badge: Badge) {
            binding.tvBadgeIcon.text = badge.icon
            binding.tvBadgeTitle.text = badge.title
            binding.tvBadgeDesc.text = badge.description
            binding.tvBadgeDate.text = "Earned: ${badge.earnedDate}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(a: Badge, b: Badge) = a.id == b.id
        override fun areContentsTheSame(a: Badge, b: Badge) = a == b
    }
}
