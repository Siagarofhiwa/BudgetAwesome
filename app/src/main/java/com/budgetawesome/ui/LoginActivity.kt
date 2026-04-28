package com.budgetawesome.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.data.entity.User
import com.budgetawesome.databinding.ActivityLoginBinding
import com.budgetawesome.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // Auto-login if session exists
        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.btnRegister.setOnClickListener { handleRegister() }
        binding.tvToggle.setOnClickListener { toggleMode() }
    }

    private var isRegisterMode = false

    private fun toggleMode() {
        isRegisterMode = !isRegisterMode
        if (isRegisterMode) {
            binding.tvTitle.text = "Create Account"
            binding.btnLogin.visibility = View.GONE
            binding.btnRegister.visibility = View.VISIBLE
            binding.tvToggle.text = "Already have an account? Sign In"
        } else {
            binding.tvTitle.text = "Welcome Back"
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.GONE
            binding.tvToggle.text = "New user? Create Account"
        }
        binding.tvError.visibility = View.GONE
    }

    private fun handleLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.")
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val user = db.userDao().login(username, password)
            runOnUiThread {
                if (user != null) {
                    session.saveUser(user.id, user.username)
                    goToMain()
                } else {
                    showError("Invalid username or password.")
                }
            }
        }
    }

    private fun handleRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.")
            return
        }
        if (password.length < 4) {
            showError("Password must be at least 4 characters.")
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val existing = db.userDao().findByUsername(username)
            runOnUiThread {
                if (existing != null) {
                    showError("Username already taken.")
                } else {
                    lifecycleScope.launch {
                        db.userDao().insert(User(username = username, password = password))
                        runOnUiThread {
                            showError("Account created! You can now sign in.")
                            toggleMode()
                        }
                    }
                }
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
