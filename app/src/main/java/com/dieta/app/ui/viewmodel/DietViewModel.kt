package com.dieta.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dieta.app.data.model.DailyLog
import com.dieta.app.data.model.DietModel
import com.dieta.app.data.model.MealTemplate
import com.dieta.app.data.model.MealWithStatus
import com.dieta.app.data.model.Patient
import com.dieta.app.data.repository.DietRepository
import com.dieta.app.pdf.MealReportEntry
import com.dieta.app.pdf.PdfReportGenerator
import com.dieta.app.util.DateMaskUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class AppScreen {
    HOME,
    MAIN_MENU,
    PATIENT,
    REPORT,
    PLAN_MANAGEMENT
}

@OptIn(ExperimentalCoroutinesApi::class)
class DietViewModel(
    private val repository: DietRepository
) : ViewModel() {

    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Current selected date
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Indicates whether the selected date is today
    val isToday: StateFlow<Boolean> = _selectedDate
        .map { it == LocalDate.now() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Formatted date string for DB query ("YYYY-MM-DD")
    val selectedDateDbStr: StateFlow<String> = _selectedDate
        .map { it.format(dbDateFormatter) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now().format(dbDateFormatter))

    // Formatted date string for display ("DD/MM/YYYY")
    val selectedDateDisplayStr: StateFlow<String> = _selectedDate
        .map { it.format(displayDateFormatter) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now().format(displayDateFormatter))

    // Patient info
    val patient: StateFlow<Patient?> = repository.patient
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Diet Model for the selected date
    val activeDietModel: StateFlow<DietModel?> = selectedDateDbStr
        .flatMapLatest { dateStr -> repository.getModelForDate(dateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Meals with completion status for selected date (includes template meals & extra meals)
    val mealsWithStatus: StateFlow<List<MealWithStatus>> = selectedDateDbStr
        .flatMapLatest { dateStr -> repository.getMealsWithStatusForDate(dateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered diet models
    val allDietModels: StateFlow<List<DietModel>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Alert flow when navigation is blocked by 5-day rule
    private val _navigationAlert = MutableSharedFlow<String>()
    val navigationAlert: SharedFlow<String> = _navigationAlert.asSharedFlow()

    // UI state for PDF generation
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    // Report filter date states (defaults to last 30 days up to today)
    private val _reportStartDate = MutableStateFlow(LocalDate.now().minusDays(30).format(dbDateFormatter))
    val reportStartDate: StateFlow<String> = _reportStartDate.asStateFlow()

    private val _reportEndDate = MutableStateFlow(LocalDate.now().format(dbDateFormatter))
    val reportEndDate: StateFlow<String> = _reportEndDate.asStateFlow()

    fun setReportDates(startDb: String, endDb: String) {
        _reportStartDate.value = startDb
        _reportEndDate.value = endDb
    }

    // Live report entries for screen preview
    val reportEntries: StateFlow<List<MealReportEntry>> = combine(
        _reportStartDate,
        _reportEndDate
    ) { start, end -> Pair(start, end) }
        .flatMapLatest { (start, end) ->
            repository.getLogsBetweenDates(start, end).map { logs ->
                val allTemplates = repository.getAllTemplatesDirect().associateBy { it.id }
                logs.map { log ->
                    val template = log.mealTemplateId?.let { allTemplates[it] }
                    MealReportEntry(
                        date = log.date,
                        mealName = if (log.isExtraMeal) log.extraMealName.ifBlank { "Refeição Avulsa" } else (template?.mealName ?: "Refeição"),
                        plannedSchedule = if (log.isExtraMeal) "" else (template?.schedule ?: "--"),
                        consumedTime = log.consumedTime,
                        suggested = template?.suggestedFoods ?: "",
                        foodEaten = log.foodEaten,
                        isExtraMeal = log.isExtraMeal
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Regra de Bloqueio de Navegação por Vigência:
     * Bloqueia avançar ou retroceder mais de 5 dias para datas sem plano alimentar vigente.
     */
    private fun canNavigateToDate(targetDate: LocalDate): Boolean {
        val targetDateStr = targetDate.format(dbDateFormatter)
        val models = allDietModels.value

        // Se o dia alvo está dentro da vigência de algum plano, a navegação é sempre permitida
        val hasActivePlan = models.any { targetDateStr >= it.startDate && targetDateStr <= it.endDate }
        if (hasActivePlan) return true

        // Se não há plano no dia alvo, calculamos a distância em dias até a vigência de plano mais próxima
        if (models.isNotEmpty()) {
            var minDistance = Long.MAX_VALUE
            for (model in models) {
                val start = LocalDate.parse(model.startDate, dbDateFormatter)
                val end = LocalDate.parse(model.endDate, dbDateFormatter)

                if (targetDate.isBefore(start)) {
                    val dist = ChronoUnit.DAYS.between(targetDate, start)
                    if (dist < minDistance) minDistance = dist
                } else if (targetDate.isAfter(end)) {
                    val dist = ChronoUnit.DAYS.between(end, targetDate)
                    if (dist < minDistance) minDistance = dist
                } else {
                    minDistance = 0
                }
            }
            return minDistance <= 5
        } else {
            // Se ainda não há nenhum plano cadastrado, limita a 5 dias a partir de hoje
            val dist = abs(ChronoUnit.DAYS.between(LocalDate.now(), targetDate))
            return dist <= 5
        }
    }

    // Navigation through dates
    fun nextDay() {
        val next = _selectedDate.value.plusDays(1)
        if (canNavigateToDate(next)) {
            _selectedDate.value = next
        } else {
            viewModelScope.launch {
                _navigationAlert.emit("Não há plano alimentar vigente para esta data")
            }
        }
    }

    fun previousDay() {
        val prev = _selectedDate.value.minusDays(1)
        if (canNavigateToDate(prev)) {
            _selectedDate.value = prev
        } else {
            viewModelScope.launch {
                _navigationAlert.emit("Não há plano alimentar vigente para esta data")
            }
        }
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun setDate(date: LocalDate) {
        if (canNavigateToDate(date)) {
            _selectedDate.value = date
        } else {
            viewModelScope.launch {
                _navigationAlert.emit("Não há plano alimentar vigente para esta data")
            }
        }
    }

    // Patient Operations
    fun savePatient(name: String, age: Int, height: Float, initialWeight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val patient = Patient(
                id = 1,
                name = name.trim(),
                age = age,
                height = height,
                initialWeight = initialWeight
            )
            repository.savePatient(patient)
        }
    }

    // Planned Meal Log Operations
    fun saveMealLog(mealTemplateId: Long, foodEaten: String, consumedTime: String) {
        val dateStr = selectedDateDbStr.value
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getLogForMealAndDateDirect(mealTemplateId, dateStr)
            val log = DailyLog(
                id = existing?.id ?: 0,
                date = dateStr,
                mealTemplateId = mealTemplateId,
                foodEaten = foodEaten.trim(),
                consumedTime = consumedTime.trim(),
                quantityWeight = "",
                isCompleted = true,
                isExtraMeal = false,
                extraMealName = ""
            )
            repository.saveDailyLog(log)
        }
    }

    // Extra Meal Operations (Refeição Avulsa do dia)
    fun saveExtraMeal(extraMealName: String, foodEaten: String, consumedTime: String, existingLogId: Long = 0) {
        val dateStr = selectedDateDbStr.value
        viewModelScope.launch(Dispatchers.IO) {
            val log = DailyLog(
                id = existingLogId,
                date = dateStr,
                mealTemplateId = null,
                foodEaten = foodEaten.trim(),
                consumedTime = consumedTime.trim(),
                quantityWeight = "",
                isCompleted = true,
                isExtraMeal = true,
                extraMealName = extraMealName.trim()
            )
            repository.saveDailyLog(log)
        }
    }

    fun deleteDailyLog(mealWithStatus: MealWithStatus) {
        val dateStr = selectedDateDbStr.value
        viewModelScope.launch(Dispatchers.IO) {
            if (mealWithStatus.isExtraMeal) {
                mealWithStatus.dailyLog?.id?.let { logId ->
                    repository.deleteDailyLogById(logId)
                }
            } else {
                mealWithStatus.template?.id?.let { templateId ->
                    repository.deleteDailyLogForMeal(templateId, dateStr)
                }
            }
        }
    }

    // Diet Model Operations (Planos Alimentares)
    fun saveModelWithTemplates(
        modelId: Long,
        title: String,
        startDate: String,
        endDate: String,
        templates: List<MealTemplate>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = DietModel(
                id = modelId,
                title = title.trim(),
                startDate = startDate.trim(),
                endDate = endDate.trim()
            )
            val savedModelId = if (modelId == 0L) {
                repository.saveModel(model)
            } else {
                repository.updateModel(model)
                modelId
            }

            // Sincroniza refeições garantindo que itens deletados sejam removidos do banco
            val updatedTemplates = templates.map { it.copy(modelId = savedModelId) }
            repository.syncModelTemplates(savedModelId, updatedTemplates)
        }
    }

    fun updateModelDates(modelId: Long, title: String, startDate: String, endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = DietModel(
                id = modelId,
                title = title,
                startDate = startDate,
                endDate = endDate
            )
            repository.updateModel(model)
        }
    }

    fun deleteModel(model: DietModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteModel(model)
        }
    }

    suspend fun getTemplatesForModel(modelId: Long): List<MealTemplate> {
        return withContext(Dispatchers.IO) {
            repository.getTemplatesForModelDirect(modelId)
        }
    }

    // PDF Report Generator
    fun generateAndSharePdfReport(
        context: Context,
        startDate: String,
        endDate: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val file = withContext(Dispatchers.IO) {
                    val patientData = repository.getPatientDirect()
                    val logs = repository.getLogsBetweenDatesDirect(startDate, endDate)
                    val allTemplates = repository.getAllTemplatesDirect().associateBy { it.id }

                    val entries = logs.map { log ->
                        val template = log.mealTemplateId?.let { allTemplates[it] }
                        MealReportEntry(
                            date = log.date,
                            mealName = if (log.isExtraMeal) log.extraMealName.ifBlank { "Refeição Avulsa" } else (template?.mealName ?: "Refeição"),
                            plannedSchedule = if (log.isExtraMeal) "" else (template?.schedule ?: "--"),
                            consumedTime = log.consumedTime,
                            suggested = template?.suggestedFoods ?: "Refeição avulsa / personalizada",
                            foodEaten = log.foodEaten,
                            isExtraMeal = log.isExtraMeal
                        )
                    }

                    val generator = PdfReportGenerator(context)
                    generator.generateReport(patientData, startDate, endDate, entries)
                }

                _isGeneratingPdf.value = false
                val generator = PdfReportGenerator(context)
                generator.openOrSharePdf(file)
                onSuccess(file)
            } catch (e: Exception) {
                _isGeneratingPdf.value = false
                onError(e.localizedMessage ?: "Erro ao gerar PDF")
            }
        }
    }
}

class DietViewModelFactory(private val repository: DietRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
