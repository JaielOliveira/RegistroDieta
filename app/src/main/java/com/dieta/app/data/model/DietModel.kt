package com.dieta.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_model")
data class DietModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startDate: String, // Formato "YYYY-MM-DD"
    val endDate: String    // Formato "YYYY-MM-DD"
)
