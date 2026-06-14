package com.budgetawesome.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.databinding.FragmentTipsBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * TipsFragment - OWN FEATURE 1: Smart Budget Tips.
 * Analyses the user's spending patterns and provides personalised financial tips.
 * Tips are generated based on: over-budget status, highest spending category,
 * number of logged expenses, and goal completion.
 * Author: Rofhiwa Siaga
 */
class TipsFragment : Fragment() {

    private val TAG = "TipsFragment"
    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Loading smart tips")
        loadTips()
    }

    /**
     * Analyses spending data and generates personalised tips.
     * Displays dynamic tips based on the user's real data from RoomDB.
     */
    private fun loadTips() {
        val session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        val userId = session.getUserId()
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val from = String.format("%d-%02d-01", year, month)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val to = String.format("%d-%02d-%02d", year, month, lastDay)

        lifecycleScope.launch {
            val expenses = db.expenseDao().getByPeriodSync(userId, from, to)
            val goal = db.goalDao().get(userId)
            val total = expenses.sumOf { it.amount }

            // Build category totals map
            val catMap = mutableMapOf<String, Double>()
            expenses.forEach { e -> catMap[e.categoryName] = (catMap[e.categoryName] ?: 0.0) + e.amount }
            val topCategory = catMap.maxByOrNull { it.value }

            val tips = mutableListOf<Pair<String, String>>() // icon to tip text

            // Tip 1: Based on budget status
            if (goal != null && goal.maxGoal > 0) {
                when {
                    total > goal.maxGoal ->
                        tips.add("🚨" to "You are over budget by ${fmt.format(total - goal.maxGoal)}. Try to reduce spending in your highest category.")
                    total > goal.maxGoal * 0.8 ->
                        tips.add("⚠️" to "You've used ${((total/goal.maxGoal)*100).toInt()}% of your budget. Slow down on non-essential spending.")
                    else ->
                        tips.add("✅" to "Great job! You've only used ${((total/goal.maxGoal)*100).toInt()}% of your budget this month.")
                }
            } else {
                tips.add("🎯" to "Set a monthly budget goal in the Goals tab to track your progress!")
            }

            // Tip 2: Top spending category
            if (topCategory != null) {
                tips.add("📊" to "Your highest spending category is '${topCategory.key}' at ${fmt.format(topCategory.value)}. Consider setting a limit for this category.")
            }

            // Tip 3: Logging consistency
            when {
                expenses.isEmpty() -> tips.add("📝" to "Start logging your expenses daily to get better insights into your spending habits.")
                expenses.size < 5 -> tips.add("💡" to "Log more expenses to unlock spending insights and earn badges!")
                else -> tips.add("🌟" to "You've logged ${expenses.size} expenses this month. Keep it up for accurate tracking!")
            }

            // Tip 4: Savings tip
            if (goal != null && goal.maxGoal > 0 && total < goal.maxGoal) {
                val saved = goal.maxGoal - total
                tips.add("💰" to "You have ${fmt.format(saved)} left in your budget. Consider saving it for next month!")
            }

            // Tip 5: General financial tip
            val generalTips = listOf(
                "💳" to "Use the 50/30/20 rule: 50% needs, 30% wants, 20% savings.",
                "🛒" to "Make a shopping list before going to the store to avoid impulse purchases.",
                "☕" to "Small daily expenses like coffee add up. Track them to see the real cost.",
                "📅" to "Review your expenses weekly to stay on top of your budget.",
                "🏦" to "Try to save at least 10% of your income each month."
            )
            tips.add(generalTips.random())

            Log.d(TAG, "loadTips: Generated ${tips.size} tips")

            requireActivity().runOnUiThread {
                binding.containerTips.removeAllViews()
                tips.forEach { (icon, text) ->
                    val tipView = layoutInflater.inflate(
                        com.budgetawesome.R.layout.item_tip,
                        binding.containerTips, false
                    )
                    tipView.findViewById<android.widget.TextView>(
                        com.budgetawesome.R.id.tvTipIcon).text = icon
                    tipView.findViewById<android.widget.TextView>(
                        com.budgetawesome.R.id.tvTipText).text = text
                    binding.containerTips.addView(tipView)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
