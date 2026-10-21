package com.dieta.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_log",
    foreignKeys = [
        ForeignKey(
            entity = MealTemplate::class,
            parentColumns = ["id"],
            childColumns = ["mealTemplateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["mealTemplateId"]),
        Index(value = ["date"]),
        Index(value = ["date", "mealTemplateId"])
    ]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                           // Formato "YYYY-MM-DD"
    val mealTemplateId: Long? = null,           // Null para refeições avulsas
    val foodEaten: String,                      // O que foi digitado pelo usuário
    val consumedTime: String = "",              // Horário real do consumo (ex: "08:15")
    val quantityWeight: String = "",            // Mantido por compatibilidade
    val isCompleted: Boolean = true,
    val isExtraMeal: Boolean = false,           // Indica refeição avulsa do dia
    val extraMealName: String = ""              // Nome da refeição avulsa (ex: "Lanche Extra")
)
