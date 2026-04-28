package com.budgetawesome.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.budgetawesome.R
import com.budgetawesome.databinding.ActivityMainBinding
import com.budgetawesome.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.tvUsername.text = "👤 ${session.getUsername()}"

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Set up bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> showFragment(DashboardFragment())
                R.id.nav_categories -> showFragment(CategoryFragment())
                R.id.nav_add -> {
                    startActivity(Intent(this, AddExpenseActivity::class.java))
                    false
                }
                R.id.nav_list -> showFragment(ExpenseListFragment())
                R.id.nav_goals -> showFragment(GoalsFragment())
                else -> false
            }.let { true }
            true
        }

        // Default fragment
        if (savedInstanceState == null) {
            showFragment(DashboardFragment())
            binding.bottomNav.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Refresh dashboard when returning from AddExpense
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (current is DashboardFragment) {
            showFragment(DashboardFragment())
        }
    }
}
