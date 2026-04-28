package com.budgetawesome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.budgetawesome.data.entity.Expense
import com.budgetawesome.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private val onDelete: (Expense) -> Unit,
    private val onViewPhoto: (String) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ViewHolder>(DiffCallback()) {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class ViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.description
            binding.tvAmount.text = fmt.format(expense.amount)
            binding.tvCategory.text = expense.categoryName
            binding.tvDate.text = "${expense.date}  ${expense.startTime} – ${expense.endTime}"

            if (expense.photoPath != null) {
                binding.btnViewPhoto.visibility = View.VISIBLE
                binding.btnViewPhoto.setOnClickListener { onViewPhoto(expense.photoPath) }
            } else {
                binding.btnViewPhoto.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDelete(expense)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
