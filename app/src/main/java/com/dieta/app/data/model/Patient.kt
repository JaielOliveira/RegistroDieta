package com.dieta.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient")
data class Patient(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val age: Int,
    val height: Float,
    val initialWeight: Float,
    val currentWeight: Float = 0f
)
