package com.budgetawesome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Badge - Entity representing a gamification badge/reward earned by the user.
 * Badges are awarded for meeting budget goals and consistent expense logging.
 * Stored in the RoomDB badges table.
 * Author: Rofhiwa Siaga
 * References: Room Entity Docs - https://developer.android.com/training/data-storage/room
 */
@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,          // Badge title e.g. "Budget Master"
    val description: String,    // What the user did to earn it
    val icon: String,           // Emoji icon for the badge
    val earnedDate: String      // Date earned in yyyy-MM-dd format
)
