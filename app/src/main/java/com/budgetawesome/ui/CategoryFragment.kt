package com.budgetawesome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgetawesome.adapter.CategoryAdapter
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.data.entity.Category
import com.budgetawesome.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter
    private val colorOptions = listOf(
        "#D4AF37", "#60A5FA", "#34D399", "#F472B6",
        "#FB923C", "#A78BFA", "#F87171", "#38BDF8"
    )
    private var selectedColor = "#D4AF37"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupColorPicker()
        setupRecycler()
        observeCategories()

        binding.btnAddCategory.setOnClickListener { addCategory() }
    }

    private fun setupColorPicker() {
        colorOptions.forEach { hex ->
            val circle = View(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(80, 80).also { it.marginEnd = 12 }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(android.graphics.Color.parseColor(hex))
                }
                setOnClickListener {
                    selectedColor = hex
                    updateColorSelection()
                }
                tag = hex
            }
            binding.colorPicker.addView(circle)
        }
        updateColorSelection()
    }

    private fun updateColorSelection() {
        for (i in 0 until binding.colorPicker.childCount) {
            val v = binding.colorPicker.getChildAt(i)
            val hex = v.tag as? String ?: continue
            (v.background as? android.graphics.drawable.GradientDrawable)?.setStroke(
                if (hex == selectedColor) 6 else 0,
                android.graphics.Color.WHITE
            )
        }
    }

    private fun setupRecycler() {
        adapter = CategoryAdapter { category ->
            val db = AppDatabase.getDatabase(requireContext())
            lifecycleScope.launch { db.categoryDao().delete(category) }
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
    }

    private fun observeCategories() {
        val db = AppDatabase.getDatabase(requireContext())
        db.categoryDao().getAllLive().observe(viewLifecycleOwner) { cats ->
            adapter.submitList(cats)
            binding.tvCatCount.text = "${cats.size} categories"
        }
    }

    private fun addCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a category name", Toast.LENGTH_SHORT).show()
            return
        }
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val existing = db.categoryDao().findByName(name)
            if (existing != null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            db.categoryDao().insert(Category(name = name, colorHex = selectedColor))
            requireActivity().runOnUiThread {
                binding.etCategoryName.setText("")
                Toast.makeText(requireContext(), "Category '$name' added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
