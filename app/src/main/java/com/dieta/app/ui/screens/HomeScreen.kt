package com.dieta.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieta.app.data.model.MealWithStatus
import com.dieta.app.ui.components.DateHeader
import com.dieta.app.ui.components.MealCard
import com.dieta.app.ui.dialogs.ExtraMealDialog
import com.dieta.app.ui.dialogs.MealLogDialog
import com.dieta.app.ui.theme.DarkBackground
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import com.dieta.app.ui.viewmodel.AppScreen
import com.dieta.app.ui.viewmodel.DietViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: DietViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Controla botão voltar do dispositivo
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (currentScreen == AppScreen.PATIENT || currentScreen == AppScreen.REPORT || currentScreen == AppScreen.PLAN_MANAGEMENT) {
            viewModel.navigateTo(AppScreen.MAIN_MENU)
        } else {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    when (currentScreen) {
        AppScreen.HOME -> HomeScreenContent(viewModel = viewModel)
        AppScreen.MAIN_MENU -> MainMenuScreen(viewModel = viewModel)
        AppScreen.PATIENT -> PatientScreen(viewModel = viewModel)
        AppScreen.REPORT -> ReportScreen(viewModel = viewModel)
        AppScreen.PLAN_MANAGEMENT -> PlanManagementScreen(viewModel = viewModel)
    }
}

@Composable
private fun HomeScreenContent(
    viewModel: DietViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados do ViewModel
    val selectedDisplayDate by viewModel.selectedDateDisplayStr.collectAsState()
    val isToday by viewModel.isToday.collectAsState()
    val activeDietModel by viewModel.activeDietModel.collectAsState()
    val mealsWithStatus by viewModel.mealsWithStatus.collectAsState()

    // Modais e diálogos
    var mealToLog by remember { mutableStateOf<MealWithStatus?>(null) }
    var showExtraMealDialog by remember { mutableStateOf(false) }
    var extraMealToEdit by remember { mutableStateOf<MealWithStatus?>(null) }

    // Alerta de bloqueio de navegação por vigência
    var blockedAlertMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.navigationAlert.collectLatest { message ->
            blockedAlertMessage = message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // 1. Cabeçalho de data com botão 'Hoje' posicionado entre data e seta '>'
            DateHeader(
                displayDate = selectedDisplayDate,
                isToday = isToday,
                onPreviousDay = { viewModel.previousDay() },
                onNextDay = { viewModel.nextDay() },
                onGoToToday = { viewModel.goToToday() },
                onDateClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
            )

            // 2. Banner de Plano Alimentar Vigente & Progresso
            if (activeDietModel != null) {
                val totalMeals = mealsWithStatus.size
                val completedMeals = mealsWithStatus.count { it.isCompleted }
                val progress = if (totalMeals > 0) completedMeals.toFloat() / totalMeals.toFloat() else 0f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeDietModel!!.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "$completedMeals/$totalMeals feitas",
                            color = if (completedMeals == totalMeals && totalMeals > 0) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VibrantBlue,
                        trackColor = Color(0xFF26262B)
                    )
                }
            } else {
                // Alerta quando não há plano alimentar vigente na data
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Não há plano alimentar vigente para esta data",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "A data $selectedDisplayDate está fora dos períodos vigentes. Você pode estender o plano atual ou cadastrar um novo.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.PLAN_MANAGEMENT) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                        ) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerenciar Planos Alimentares", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Botão para Adicionar Refeição Avulsa no Dia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showExtraMealDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF59E0B)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Refeição Avulsa",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Lista de Refeições
            if (mealsWithStatus.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma refeição registrada para hoje.\nUse o botão acima para adicionar uma refeição avulsa ou configure seu plano alimentar.",
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = mealsWithStatus,
                        key = { it.id }
                    ) { mealWithStatus ->
                        MealCard(
                            mealWithStatus = mealWithStatus,
                            onAddOrEditClick = {
                                if (mealWithStatus.isExtraMeal) {
                                    extraMealToEdit = mealWithStatus
                                    showExtraMealDialog = true
                                } else {
                                    mealToLog = mealWithStatus
                                }
                            },
                            onDeleteClick = if (mealWithStatus.isCompleted) {
                                {
                                    viewModel.deleteDailyLog(mealWithStatus)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Registro removido.")
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    // Modal: Registro de Refeição Planejada
    mealToLog?.let { currentMeal ->
        MealLogDialog(
            mealWithStatus = currentMeal,
            onDismiss = { mealToLog = null },
            onSave = { foodEaten, consumedTime ->
                viewModel.saveMealLog(
                    mealTemplateId = currentMeal.template!!.id,
                    foodEaten = foodEaten,
                    consumedTime = consumedTime
                )
                mealToLog = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Refeição registrada com sucesso!")
                }
            },
            onDelete = if (currentMeal.dailyLog != null) {
                {
                    viewModel.deleteDailyLog(currentMeal)
                    mealToLog = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Registro removido.")
                    }
                }
            } else null
        )
    }

    // Modal: Registro de Refeição Avulsa (Extra)
    if (showExtraMealDialog) {
        ExtraMealDialog(
            existingMeal = extraMealToEdit,
            onDismiss = {
                showExtraMealDialog = false
                extraMealToEdit = null
            },
            onSave = { mealName, foodEaten, consumedTime, logId ->
                viewModel.saveExtraMeal(
                    extraMealName = mealName,
                    foodEaten = foodEaten,
                    consumedTime = consumedTime,
                    existingLogId = logId
                )
                showExtraMealDialog = false
                extraMealToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Refeição avulsa salva!")
                }
            },
            onDelete = if (extraMealToEdit != null) {
                {
                    viewModel.deleteDailyLog(extraMealToEdit!!)
                    showExtraMealDialog = false
                    extraMealToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Refeição avulsa removida.")
                    }
                }
            } else null
        )
    }

    // Dialog de Alerta de Bloqueio por Vigência (Regra dos 5 dias)
    blockedAlertMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { blockedAlertMessage = null },
            confirmButton = {
                TextButton(onClick = { blockedAlertMessage = null }) {
                    Text("Entendido", color = VibrantBlue, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aviso de Navegação", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            },
            containerColor = DarkCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
