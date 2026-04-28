package com.budgetawesome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,           // yyyy-MM-dd
    val startTime: String,      // HH:mm
    val endTime: String,        // HH:mm
    val description: String,
    val amount: Double,
    val categoryId: Int,
    val categoryName: String,
    val photoPath: String? = null,
    val userId: Int
)
