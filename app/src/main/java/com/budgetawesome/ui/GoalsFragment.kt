package com.budgetawesome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.data.entity.Goal
import com.budgetawesome.databinding.FragmentGoalsBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class GoalsFragment : Fragment() {
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        val userId = session.getUserId()
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

        // Load existing goals
        db.goalDao().getLive(userId).observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                binding.etMinGoal.setText(if (goal.minGoal > 0) goal.minGoal.toString() else "")
                binding.etMaxGoal.setText(if (goal.maxGoal > 0) goal.maxGoal.toString() else "")
                binding.tvCurrentMin.text = "Current Min: ${fmt.format(goal.minGoal)}"
                binding.tvCurrentMax.text = "Current Max: ${fmt.format(goal.maxGoal)}"
            }
        }

        // Show this month's spending
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val from = String.format("%d-%02d-01", year, month)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val to = String.format("%d-%02d-%02d", year, month, lastDay)

        lifecycleScope.launch {
            val total = db.expenseDao().getByPeriodSync(userId, from, to).sumOf { it.amount }
            requireActivity().runOnUiThread {
                binding.tvMonthSpent.text = "This month: ${fmt.format(total)}"
            }
        }

        binding.btnSaveGoals.setOnClickListener {
            val minStr = binding.etMinGoal.text.toString().trim()
            val maxStr = binding.etMaxGoal.text.toString().trim()

            val min = minStr.toDoubleOrNull() ?: 0.0
            val max = maxStr.toDoubleOrNull() ?: 0.0

            if (min < 0 || max < 0) {
                Toast.makeText(requireContext(), "Goals cannot be negative", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (max > 0 && min > max) {
                Toast.makeText(requireContext(), "Min goal cannot exceed max goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.goalDao().insertOrUpdate(Goal(userId = userId, minGoal = min, maxGoal = max))
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Goals saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
