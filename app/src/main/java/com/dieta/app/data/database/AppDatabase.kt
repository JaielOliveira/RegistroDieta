package com.dieta.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dieta.app.data.dao.DailyLogDao
import com.dieta.app.data.dao.DietModelDao
import com.dieta.app.data.dao.MealTemplateDao
import com.dieta.app.data.dao.PatientDao
import com.dieta.app.data.model.DailyLog
import com.dieta.app.data.model.DietModel
import com.dieta.app.data.model.MealTemplate
import com.dieta.app.data.model.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Patient::class,
        DietModel::class,
        MealTemplate::class,
        DailyLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun dietModelDao(): DietModelDao
    abstract fun mealTemplateDao(): MealTemplateDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dieta_database.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val patientDao = database.patientDao()
                val dietModelDao = database.dietModelDao()
                val mealTemplateDao = database.mealTemplateDao()

                // 1. Initial Patient
                patientDao.insertOrUpdate(
                    Patient(
                        id = 1,
                        name = "Paciente",
                        age = 30,
                        height = 1.75f,
                        initialWeight = 78.5f,
                        currentWeight = 76.2f
                    )
                )

                // 2. Initial Diet Model (matches prompt example: 15/10/2026 to 14/11/2026)
                val modelId = dietModelDao.insert(
                    DietModel(
                        id = 0,
                        title = "Plano Alimentar - Fase 1",
                        startDate = "2026-10-15",
                        endDate = "2026-11-14"
                    )
                )

                // 3. Initial Meal Templates
                val initialTemplates = listOf(
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Café da manhã",
                        schedule = "08:00",
                        suggestedFoods = "Café com leite desnatado (200ml) + Cuscuz (150g) com 2 ovos mexidos",
                        substitutions = "Tapioca com queijo branco ou 2 fatias de pão integral com ovos mexidos"
                    ),
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Lanche da manhã",
                        schedule = "10:30",
                        suggestedFoods = "1 maçã ou 1 banana + 20g de castanhas",
                        substitutions = "Iogurte natural desnatado com sementes de chia"
                    ),
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Almoço",
                        schedule = "12:30",
                        suggestedFoods = "Arroz integral (100g) + Feijão (80g) + Peito de frango grelhado (150g) + Salada à vontade",
                        substitutions = "Carne magra grelhada (150g) ou Filé de peixe (160g) com batata doce (120g)"
                    ),
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Lanche da tarde",
                        schedule = "16:00",
                        suggestedFoods = "Vitamina de frutas com aveia e whey protein",
                        substitutions = "Crepioca de 1 ovo com 1 colher de sopa de goma e queijo cottage"
                    ),
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Jantar",
                        schedule = "19:30",
                        suggestedFoods = "Sopa de legumes com carne magra ou Prato de salada com frango desfiado (130g)",
                        substitutions = "Repetir o almoço reduzindo a porção de carboidrato pela metade"
                    ),
                    MealTemplate(
                        modelId = modelId,
                        mealName = "Ceia",
                        schedule = "22:00",
                        suggestedFoods = "Chá de camomila ou erva-doce + 3 nozes",
                        substitutions = "1 fatia fina de queijo minas frescal"
                    )
                )
                mealTemplateDao.insertAll(initialTemplates)
            }
        }
    }
}
