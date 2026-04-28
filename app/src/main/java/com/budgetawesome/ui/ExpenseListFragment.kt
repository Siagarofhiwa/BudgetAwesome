package com.budgetawesome.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetawesome.adapter.ExpenseAdapter
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.databinding.FragmentExpenseListBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ExpenseListFragment : Fragment() {
    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter
    private var fromDate = ""
    private var toDate = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Default: current month
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        fromDate = String.format("%d-%02d-01", year, month)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        toDate = String.format("%d-%02d-%02d", year, month, lastDay)

        updateDateDisplay()
        setupRecycler()
        loadExpenses()

        binding.btnFromDate.setOnClickListener { pickDate(true) }
        binding.btnToDate.setOnClickListener { pickDate(false) }
    }

    private fun setupRecycler() {
        val session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        adapter = ExpenseAdapter(
            onDelete = { expense ->
                lifecycleScope.launch { db.expenseDao().delete(expense) }
                loadExpenses()
            },
            onViewPhoto = { path ->
                val intent = android.content.Intent(requireContext(), PhotoViewActivity::class.java)
                intent.putExtra("photo_path", path)
                startActivity(intent)
            }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun loadExpenses() {
        val session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        db.expenseDao().getByPeriodLive(session.getUserId(), fromDate, toDate)
            .observe(viewLifecycleOwner) { expenses ->
                adapter.submitList(expenses)
                val total = expenses.sumOf { it.amount }
                val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                binding.tvTotal.text = "Total: ${fmt.format(total)}"
                binding.tvCount.text = "${expenses.size} entries"
                binding.tvNoExpenses.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun pickDate(isFrom: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val date = String.format("%d-%02d-%02d", y, m + 1, d)
            if (isFrom) fromDate = date else toDate = date
            updateDateDisplay()
            loadExpenses()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateDisplay() {
        binding.tvFromDate.text = fromDate
        binding.tvToDate.text = toDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
