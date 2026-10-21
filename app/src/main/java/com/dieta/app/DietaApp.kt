package com.dieta.app

import android.app.Application
import com.dieta.app.data.database.AppDatabase
import com.dieta.app.data.repository.DietRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DietaApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val repository by lazy {
        DietRepository(
            patientDao = database.patientDao(),
            dietModelDao = database.dietModelDao(),
            mealTemplateDao = database.mealTemplateDao(),
            dailyLogDao = database.dailyLogDao()
        )
    }
}
