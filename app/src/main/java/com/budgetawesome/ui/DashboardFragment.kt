package com.budgetawesome.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.databinding.FragmentDashboardBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        val session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        val userId = session.getUserId()

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val from = String.format("%d-%02d-01", year, month)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val to = String.format("%d-%02d-%02d", year, month, lastDay)

        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        binding.tvMonth.text = "$monthName $year"

        lifecycleScope.launch {
            val expenses = db.expenseDao().getByPeriodSync(userId, from, to)
            val goal = db.goalDao().get(userId)
            val categories = db.categoryDao().getAll()

            val total = expenses.sumOf { it.amount }
            val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

            requireActivity().runOnUiThread {
                binding.tvTotalSpent.text = fmt.format(total)
                binding.tvEntryCount.text = "${expenses.size} entries"
                binding.tvCategoryCount.text = "${categories.size} categories"

                // Goals progress
                if (goal != null && goal.maxGoal > 0) {
                    binding.layoutGoals.visibility = View.VISIBLE
                    val pct = ((total / goal.maxGoal) * 100).toInt().coerceIn(0, 100)
                    binding.progressMax.progress = pct
                    binding.tvGoalMax.text = "Max: ${fmt.format(goal.maxGoal)} | Spent: ${fmt.format(total)}"
                    binding.progressMax.progressTintList = android.content.res.ColorStateList.valueOf(
                        if (total > goal.maxGoal) Color.parseColor("#EF4444") else Color.parseColor("#D4AF37")
                    )
                    if (total > goal.maxGoal) {
                        binding.tvOverBudget.visibility = View.VISIBLE
                        binding.tvOverBudget.text = "⚠️ Over budget by ${fmt.format(total - goal.maxGoal)}"
                    } else {
                        binding.tvOverBudget.visibility = View.GONE
                    }
                    if (goal.minGoal > 0) {
                        val minPct = ((total / goal.minGoal) * 100).toInt().coerceIn(0, 100)
                        binding.progressMin.progress = minPct
                        binding.tvGoalMin.text = "Min: ${fmt.format(goal.minGoal)}"
                        binding.layoutMinGoal.visibility = View.VISIBLE
                    }
                } else {
                    binding.layoutGoals.visibility = View.GONE
                }

                // Category breakdown
                val catMap = mutableMapOf<String, Double>()
                expenses.forEach { e -> catMap[e.categoryName] = (catMap[e.categoryName] ?: 0.0) + e.amount }

                if (catMap.isEmpty()) {
                    binding.layoutCatBreakdown.visibility = View.GONE
                    binding.tvNoExpenses.visibility = View.VISIBLE
                } else {
                    binding.layoutCatBreakdown.visibility = View.VISIBLE
                    binding.tvNoExpenses.visibility = View.GONE
                    binding.containerCategories.removeAllViews()
                    catMap.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                        val pct = if (total > 0) ((amt / total) * 100).toInt() else 0
                        val row = layoutInflater.inflate(
                            com.budgetawesome.R.layout.item_category_stat,
                            binding.containerCategories, false
                        )
                        row.findViewById<android.widget.TextView>(com.budgetawesome.R.id.tvCatName).text = cat
                        row.findViewById<android.widget.TextView>(com.budgetawesome.R.id.tvCatAmount).text =
                            "${fmt.format(amt)} ($pct%)"
                        val pb = row.findViewById<android.widget.ProgressBar>(com.budgetawesome.R.id.pbCat)
                        pb.progress = pct
                        binding.containerCategories.addView(row)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
