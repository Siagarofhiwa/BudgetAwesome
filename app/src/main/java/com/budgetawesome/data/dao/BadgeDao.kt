package com.budgetawesome.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgetawesome.data.entity.Badge

/**
 * BadgeDao - Data Access Object for the badges table.
 * Provides methods to insert and retrieve badges for gamification.
 * Author: Rofhiwa Siaga
 */
@Dao
interface BadgeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(badge: Badge)

    // Get all badges for a specific user as LiveData (auto-updates UI)
    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedDate DESC")
    fun getAllForUser(userId: Int): LiveData<List<Badge>>

    // Get all badges as a regular list for background processing
    @Query("SELECT * FROM badges WHERE userId = :userId")
    suspend fun getAllForUserSync(userId: Int): List<Badge>

    // Check if a specific badge title already exists for this user (prevent duplicates)
    @Query("SELECT * FROM badges WHERE userId = :userId AND title = :title LIMIT 1")
    suspend fun findByTitle(userId: Int, title: String): Badge?
}
