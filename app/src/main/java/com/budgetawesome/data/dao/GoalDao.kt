package com.budgetawesome.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgetawesome.data.entity.Goal

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId LIMIT 1")
    fun getLive(userId: Int): LiveData<Goal?>

    @Query("SELECT * FROM goals WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: Int): Goal?
}
