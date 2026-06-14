package com.budgetawesome.util

import android.util.Log
import com.budgetawesome.data.AppDatabase
import com.budgetawesome.data.entity.Badge
import java.text.SimpleDateFormat
import java.util.*

/**
 * BadgeEngine - The gamification engine that checks conditions and awards badges.
 * Called after any expense is saved or when the Badges screen is opened.
 * Awards badges for: first expense, staying within budget, logging 5/10 expenses,
 * creating categories, and meeting the minimum spending goal.
 * Author: Rofhiwa Siaga
 * References: Gamification in Apps - https://developer.android.com/games
 */
object BadgeEngine {

    private const val TAG = "BadgeEngine"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Main entry point - checks all badge conditions and awards any that are newly earned.
     * Safe to call multiple times - won't award the same badge twice.
     * @param db The AppDatabase instance
     * @param userId The current user's ID
     */
    suspend fun checkAndAwardBadges(db: AppDatabase, userId: Int) {
        Log.d(TAG, "checkAndAwardBadges: Checking badges for userId=$userId")

        val expenses = db.expenseDao().getAllSync(userId)
        val goal = db.goalDao().get(userId)
        val today = dateFormat.format(Date())

        // Get current month range
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val from = String.format("%d-%02d-01", year, month)
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val to = String.format("%d-%02d-%02d", year, month, lastDay)
        val monthTotal = db.expenseDao().getByPeriodSync(userId, from, to).sumOf { it.amount }

        // 🏅 Badge 1: First Steps - Log your first expense
        if (expenses.isNotEmpty()) {
            awardIfNew(db, userId, "First Steps", "Logged your very first expense!", "🏅", today)
        }

        // 🌟 Badge 2: Expense Explorer - Log 5 or more expenses
        if (expenses.size >= 5) {
            awardIfNew(db, userId, "Expense Explorer", "Logged 5 expenses!", "🌟", today)
        }

        // 💎 Badge 3: Power Tracker - Log 10 or more expenses
        if (expenses.size >= 10) {
            awardIfNew(db, userId, "Power Tracker", "Logged 10 expenses!", "💎", today)
        }

        // 🎯 Badge 4: Goal Setter - Set a budget goal
        if (goal != null && goal.maxGoal > 0) {
            awardIfNew(db, userId, "Goal Setter", "Set your first budget goal!", "🎯", today)
        }

        // 💚 Badge 5: Budget Hero - Stay within max budget for the month
        if (goal != null && goal.maxGoal > 0 && monthTotal > 0 && monthTotal <= goal.maxGoal) {
            awardIfNew(db, userId, "Budget Hero", "Stayed within budget this month!", "💚", today)
        }

        // 🏆 Badge 6: Savings Champion - Spend less than min goal
        if (goal != null && goal.minGoal > 0 && monthTotal > 0 && monthTotal <= goal.minGoal) {
            awardIfNew(db, userId, "Savings Champion", "Spent under your minimum goal!", "🏆", today)
        }

        // 📸 Badge 7: Photogenic - Attach a photo to an expense
        val hasPhoto = expenses.any { it.photoPath != null }
        if (hasPhoto) {
            awardIfNew(db, userId, "Photogenic", "Attached a photo to an expense!", "📸", today)
        }

        // 🗂️ Badge 8: Organised - Create 3 or more categories
        val catCount = db.categoryDao().getAll().size
        if (catCount >= 3) {
            awardIfNew(db, userId, "Organised", "Created 3 or more categories!", "🗂️", today)
        }

        Log.d(TAG, "checkAndAwardBadges: Done")
    }

    /**
     * Awards a badge only if the user hasn't already earned it.
     * Prevents duplicate badges from being inserted.
     */
    private suspend fun awardIfNew(
        db: AppDatabase, userId: Int,
        title: String, description: String,
        icon: String, date: String
    ) {
        val existing = db.badgeDao().findByTitle(userId, title)
        if (existing == null) {
            db.badgeDao().insert(
                Badge(
                    userId = userId,
                    title = title,
                    description = description,
                    icon = icon,
                    earnedDate = date
                )
            )
            Log.i(TAG, "awardIfNew: Awarded badge '$title' to userId=$userId")
        }
    }
}
