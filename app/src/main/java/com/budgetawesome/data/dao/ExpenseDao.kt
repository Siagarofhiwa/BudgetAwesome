package com.budgetawesome.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgetawesome.data.entity.Expense

/**
 * ExpenseDao - Data Access Object for the expenses table.
 * Provides methods to insert, delete and query expenses.
 * Author: Rofhiwa Siaga
 * References: Room DAO - https://developer.android.com/training/data-storage/room/accessing-data
 */
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    // All expenses for a user as LiveData
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllLive(userId: Int): LiveData<List<Expense>>

    // All expenses for a user as a plain list - used by SearchFragment and BadgeEngine
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllSync(userId: Int): List<Expense>

    // Expenses filtered by date period as LiveData - used by ExpenseListFragment
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC, startTime DESC")
    fun getByPeriod(userId: Int, from: String, to: String): LiveData<List<Expense>>

    // Expenses filtered by period as plain list - used by Dashboard, Goals, BadgeEngine
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getByPeriodSync(userId: Int, from: String, to: String): List<Expense>

    // Expenses filtered by period as LiveData - used by ExpenseListFragment observer
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    fun getByPeriodLive(userId: Int, from: String, to: String): LiveData<List<Expense>>
}