package com.dieta.app.data.repository

import com.dieta.app.data.dao.DailyLogDao
import com.dieta.app.data.dao.DietModelDao
import com.dieta.app.data.dao.MealTemplateDao
import com.dieta.app.data.dao.PatientDao
import com.dieta.app.data.model.DailyLog
import com.dieta.app.data.model.DietModel
import com.dieta.app.data.model.MealTemplate
import com.dieta.app.data.model.MealWithStatus
import com.dieta.app.data.model.Patient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DietRepository(
    private val patientDao: PatientDao,
    private val dietModelDao: DietModelDao,
    private val mealTemplateDao: MealTemplateDao,
    private val dailyLogDao: DailyLogDao
) {
    // Patient operations
    val patient: Flow<Patient?> = patientDao.getPatient()

    suspend fun getPatientDirect(): Patient? = patientDao.getPatientDirect()

    suspend fun savePatient(patient: Patient) {
        patientDao.insertOrUpdate(patient)
    }

    // Diet Model operations
    fun getModelForDate(date: String): Flow<DietModel?> = dietModelDao.getModelForDate(date)

    suspend fun getModelForDateDirect(date: String): DietModel? = dietModelDao.getModelForDateDirect(date)

    val allModels: Flow<List<DietModel>> = dietModelDao.getAllModels()

    suspend fun getModelById(id: Long): DietModel? = dietModelDao.getModelById(id)

    suspend fun saveModel(model: DietModel): Long = dietModelDao.insert(model)

    suspend fun updateModel(model: DietModel) = dietModelDao.update(model)

    suspend fun deleteModel(model: DietModel) = dietModelDao.delete(model)

    // Meal Template operations
    fun getTemplatesForModel(modelId: Long): Flow<List<MealTemplate>> =
        mealTemplateDao.getTemplatesForModel(modelId)

    suspend fun getTemplatesForModelDirect(modelId: Long): List<MealTemplate> =
        mealTemplateDao.getTemplatesForModelDirect(modelId)

    suspend fun getTemplateById(id: Long): MealTemplate? = mealTemplateDao.getTemplateById(id)

    suspend fun getAllTemplatesDirect(): List<MealTemplate> = mealTemplateDao.getAllTemplatesDirect()

    suspend fun saveTemplate(template: MealTemplate): Long = mealTemplateDao.insert(template)

    suspend fun saveTemplates(templates: List<MealTemplate>) = mealTemplateDao.insertAll(templates)

    suspend fun updateTemplate(template: MealTemplate) = mealTemplateDao.update(template)

    suspend fun deleteTemplate(template: MealTemplate) = mealTemplateDao.delete(template)

    /**
     * Sincronização precisa de refeições do plano:
     * Remove do banco de dados qualquer refeição que tenha sido excluída pelo usuário na tela de edição.
     */
    suspend fun syncModelTemplates(modelId: Long, currentTemplates: List<MealTemplate>) {
        val existing = mealTemplateDao.getTemplatesForModelDirect(modelId)
        val currentIds = currentTemplates.map { it.id }.filter { it > 0 }.toSet()
        val toDelete = existing.filter { it.id !in currentIds }
        for (del in toDelete) {
            mealTemplateDao.delete(del)
        }
        for (template in currentTemplates) {
            if (template.id == 0L) {
                mealTemplateDao.insert(template.copy(modelId = modelId))
            } else {
                mealTemplateDao.update(template.copy(modelId = modelId))
            }
        }
    }

    // Daily Log operations
    fun getLogsForDate(date: String): Flow<List<DailyLog>> = dailyLogDao.getLogsForDate(date)

    fun getLogForMealAndDate(templateId: Long, date: String): Flow<DailyLog?> =
        dailyLogDao.getLogForMealAndDate(templateId, date)

    suspend fun getLogForMealAndDateDirect(templateId: Long, date: String): DailyLog? =
        dailyLogDao.getLogForMealAndDateDirect(templateId, date)

    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<DailyLog>> =
        dailyLogDao.getLogsBetweenDates(startDate, endDate)

    suspend fun getLogsBetweenDatesDirect(startDate: String, endDate: String): List<DailyLog> =
        dailyLogDao.getLogsBetweenDatesDirect(startDate, endDate)

    suspend fun saveDailyLog(log: DailyLog): Long = dailyLogDao.insertOrUpdate(log)

    suspend fun deleteDailyLog(log: DailyLog) = dailyLogDao.delete(log)

    suspend fun deleteDailyLogById(id: Long) = dailyLogDao.deleteById(id)

    suspend fun deleteDailyLogForMeal(templateId: Long, date: String) =
        dailyLogDao.deleteForMealAndDate(templateId, date)

    /**
     * Fluxo reativo para a data selecionada:
     * 1. Consulta o modelo de dieta vigente para a data.
     * 2. Combina os modelos de refeição previstos com os registros diários já realizados.
     * 3. Anexa também quaisquer refeições avulsas registradas exclusivamente para aquele dia.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMealsWithStatusForDate(date: String): Flow<List<MealWithStatus>> {
        return dietModelDao.getModelForDate(date).flatMapLatest { model ->
            if (model == null) {
                // Se não há modelo ativo, ainda exibe refeições avulsas registradas no dia
                dailyLogDao.getLogsForDate(date).map { logs ->
                    logs.filter { it.isExtraMeal }.map { extraLog ->
                        MealWithStatus(template = null, dailyLog = extraLog)
                    }
                }
            } else {
                combine(
                    mealTemplateDao.getTemplatesForModel(model.id),
                    dailyLogDao.getLogsForDate(date)
                ) { templates, logs ->
                    val logsByTemplateId = logs.filter { !it.isExtraMeal && it.mealTemplateId != null }
                        .associateBy { it.mealTemplateId!! }

                    val templateMeals = templates.map { template ->
                        MealWithStatus(
                            template = template,
                            dailyLog = logsByTemplateId[template.id]
                        )
                    }

                    val extraMeals = logs.filter { it.isExtraMeal }.map { extraLog ->
                        MealWithStatus(template = null, dailyLog = extraLog)
                    }

                    templateMeals + extraMeals
                }
            }
        }
    }
}
