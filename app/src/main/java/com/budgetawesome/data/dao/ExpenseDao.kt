package com.budgetawesome.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgetawesome.data.entity.Expense

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllLive(userId: Int): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC, startTime DESC")
    fun getByPeriod(userId: Int, from: String, to: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getByPeriodSync(userId: Int, from: String, to: String): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date DESC")
    fun getByPeriodLive(userId: Int, from: String, to: String): LiveData<List<Expense>>
}
