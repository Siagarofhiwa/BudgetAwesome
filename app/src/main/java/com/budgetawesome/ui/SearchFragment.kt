package com.budgetawesome.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetawesome.adapter.ExpenseAdapter
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.databinding.FragmentSearchBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

/**
 * SearchFragment - OWN FEATURE 2: Search and Filter Expenses.
 * Allows users to search expenses by description or category name in real time.
 * Results update as the user types. Shows total of filtered results.
 * Author: Rofhiwa Siaga
 */
class SearchFragment : Fragment() {

    private val TAG = "SearchFragment"
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up SearchFragment")

        val db = AppDatabase.getDatabase(requireContext())
        val session = SessionManager(requireContext())
        val userId = session.getUserId()

        // Set up RecyclerView
        adapter = ExpenseAdapter(
            onDelete = { expense ->
                lifecycleScope.launch { db.expenseDao().delete(expense) }
            },
            onViewPhoto = { path ->
                val intent = android.content.Intent(requireContext(), PhotoViewActivity::class.java)
                intent.putExtra("photo_path", path)
                startActivity(intent)
            }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter

        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

        // Real-time search as user types
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim().lowercase()
            Log.d(TAG, "Search query: '$query'")

            lifecycleScope.launch {
                val allExpenses = db.expenseDao().getAllSync(userId)
                val filtered = if (query.isEmpty()) {
                    allExpenses
                } else {
                    allExpenses.filter { expense ->
                        expense.description.lowercase().contains(query) ||
                        expense.categoryName.lowercase().contains(query)
                    }
                }

                val total = filtered.sumOf { it.amount }
                Log.d(TAG, "Search results: ${filtered.size} matching expenses")

                requireActivity().runOnUiThread {
                    adapter.submitList(filtered)
                    binding.tvResultCount.text = "${filtered.size} results | Total: ${fmt.format(total)}"
                    binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Load all expenses on start
        lifecycleScope.launch {
            val all = db.expenseDao().getAllSync(userId)
            val total = all.sumOf { it.amount }
            requireActivity().runOnUiThread {
                adapter.submitList(all)
                binding.tvResultCount.text = "${all.size} expenses | Total: ${fmt.format(total)}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
