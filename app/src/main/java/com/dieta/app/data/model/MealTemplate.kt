package com.dieta.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_template",
    foreignKeys = [
        ForeignKey(
            entity = DietModel::class,
            parentColumns = ["id"],
            childColumns = ["modelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["modelId"])]
)
data class MealTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelId: Long,
    val mealName: String,       // Ex: "Café da manhã", "Almoço", "Jantar"
    val schedule: String,       // Ex: "8h00", "Entre 11 e 13 horas", "19:00 horas"
    val suggestedFoods: String, // Ex: "Café com leite + Cuscuz..."
    val substitutions: String   // Ex: "Ou repetir café da manhã"
)
