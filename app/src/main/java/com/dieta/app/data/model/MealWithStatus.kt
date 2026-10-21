package com.dieta.app.data.model

data class MealWithStatus(
    val template: MealTemplate? = null,
    val dailyLog: DailyLog? = null
) {
    val id: Long
        get() = template?.id ?: -(dailyLog?.id ?: 1L)

    val mealName: String
        get() = template?.mealName ?: dailyLog?.extraMealName?.ifBlank { "Refeição Avulsa" } ?: "Refeição Avulsa"

    val plannedSchedule: String
        get() = template?.schedule ?: ""

    val consumedTime: String
        get() = dailyLog?.consumedTime ?: ""

    val suggestedFoods: String
        get() = template?.suggestedFoods ?: ""

    val substitutions: String
        get() = template?.substitutions ?: ""

    val foodEaten: String
        get() = dailyLog?.foodEaten ?: ""

    val isExtraMeal: Boolean
        get() = dailyLog?.isExtraMeal == true || template == null

    val isCompleted: Boolean
        get() = dailyLog != null && dailyLog.isCompleted
}
