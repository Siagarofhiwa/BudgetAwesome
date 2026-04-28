package com.budgetawesome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val userId: Int,
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0
)
