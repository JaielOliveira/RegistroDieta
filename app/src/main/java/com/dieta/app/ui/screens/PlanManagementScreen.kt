package com.dieta.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dieta.app.data.model.DietModel
import com.dieta.app.data.model.MealTemplate
import com.dieta.app.ui.theme.DarkBackground
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import com.dieta.app.ui.viewmodel.AppScreen
import com.dieta.app.ui.viewmodel.DietViewModel
import com.dieta.app.util.DateMaskUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanManagementScreen(
    viewModel: DietViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allModels by viewModel.allDietModels.collectAsState()
    val currentDbDate by viewModel.selectedDateDbStr.collectAsState()

    var editingModel by remember { mutableStateOf<DietModel?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingModel != null) "Editar Plano Alimentar" else if (isCreatingNew) "Novo Plano Alimentar" else "Planos Alimentares",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (editingModel != null || isCreatingNew) {
                                editingModel = null
                                isCreatingNew = false
                            } else {
                                viewModel.navigateTo(AppScreen.MAIN_MENU)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (editingModel != null || isCreatingNew) {
                PlanEditorView(
                    model = editingModel,
                    onBack = {
                        editingModel = null
                        isCreatingNew = false
                    },
                    onSave = { modelId, title, startDb, endDb, templates ->
                        viewModel.saveModelWithTemplates(modelId, title, startDb, endDb, templates)
                        editingModel = null
                        isCreatingNew = false
                        Toast.makeText(context, "Plano Alimentar salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    loadTemplatesForModel = { id -> viewModel.getTemplatesForModel(id) }
                )
            } else {
                PlanListView(
                    models = allModels,
                    currentDateDb = currentDbDate,
                    onCreateNew = { isCreatingNew = true },
                    onEdit = { editingModel = it },
                    onDelete = { modelToDelete ->
                        viewModel.deleteModel(modelToDelete)
                        Toast.makeText(context, "Plano Alimentar excluído.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanListView(
    models: List<DietModel>,
    currentDateDb: String,
    onCreateNew: () -> Unit,
    onEdit: (DietModel) -> Unit,
    onDelete: (DietModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botão Novo Plano Alimentar
        Button(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Criar Novo Plano Alimentar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (models.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum plano alimentar cadastrado.\nClique acima para criar o seu primeiro plano.",
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(models) { model ->
                    val isActive = currentDateDb >= model.startDate && currentDateDb <= model.endDate

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFF1E3A8A) else DarkCardBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 17.sp
                                        )
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF10B981))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Vigente hoje",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val startDisplay = DateMaskUtils.convertDbDateToDisplayDate(model.startDate)
                                        val endDisplay = DateMaskUtils.convertDbDateToDisplayDate(model.endDate)
                                        Text(
                                            text = "Vigência: $startDisplay até $endDisplay",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { onEdit(model) }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { onDelete(model) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Excluir",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanEditorView(
    model: DietModel?,
    onBack: () -> Unit,
    onSave: (modelId: Long, title: String, startDateDb: String, endDateDb: String, templates: List<MealTemplate>) -> Unit,
    loadTemplatesForModel: suspend (Long) -> List<MealTemplate>
) {
    val modelId = model?.id ?: 0L
    var title by remember { mutableStateOf(model?.title ?: "") }

    var startDisplay by remember {
        mutableStateOf(DateMaskUtils.convertDbDateToDisplayDate(model?.startDate ?: "2026-10-15"))
    }
    var endDisplay by remember {
        mutableStateOf(DateMaskUtils.convertDbDateToDisplayDate(model?.endDate ?: "2026-11-14"))
    }

    val templates = remember { mutableStateListOf<MealTemplate>() }
    var templateBeingEdited by remember { mutableStateOf<MealTemplate?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(modelId) {
        if (modelId != 0L) {
            val list = loadTemplatesForModel(modelId)
            templates.clear()
            templates.addAll(list)
        } else {
            templates.clear()
            templates.addAll(
                listOf(
                    MealTemplate(modelId = 0, mealName = "Café da manhã", schedule = "08:00", suggestedFoods = "", substitutions = ""),
                    MealTemplate(modelId = 0, mealName = "Almoço", schedule = "12:30", suggestedFoods = "", substitutions = ""),
                    MealTemplate(modelId = 0, mealName = "Lanche da tarde", schedule = "16:00", suggestedFoods = "", substitutions = ""),
                    MealTemplate(modelId = 0, mealName = "Jantar", schedule = "19:30", suggestedFoods = "", substitutions = "")
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nome do Plano Alimentar
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nome do Plano Alimentar", color = Color.White.copy(alpha = 0.7f)) },
            placeholder = { Text("Ex: Plano Alimentar - Fase 1", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = VibrantBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = VibrantBlue
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Datas de Vigência com máscara automática DD/MM/AAAA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = startDisplay,
                onValueChange = {
                    startDisplay = DateMaskUtils.formatDateInput(it)
                },
                label = { Text("Início (DD/MM/AAAA)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                placeholder = { Text("15/10/2026", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = VibrantBlue
                )
            )

            OutlinedTextField(
                value = endDisplay,
                onValueChange = {
                    endDisplay = DateMaskUtils.formatDateInput(it)
                },
                label = { Text("Término (DD/MM/AAAA)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                placeholder = { Text("14/11/2026", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = VibrantBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botões para estender vigência até a próxima consulta nutricional
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    DateMaskUtils.convertDisplayDateToDbDate(endDisplay)?.let { curEndDb ->
                        try {
                            val end = LocalDate.parse(curEndDb)
                            val newEnd = end.plusDays(15).toString()
                            endDisplay = DateMaskUtils.convertDbDateToDisplayDate(newEnd)
                        } catch (e: Exception) {}
                    }
                }
            ) {
                Text("+15 dias de vigência", color = VibrantBlue, fontSize = 12.sp)
            }

            TextButton(
                onClick = {
                    DateMaskUtils.convertDisplayDateToDbDate(endDisplay)?.let { curEndDb ->
                        try {
                            val end = LocalDate.parse(curEndDb)
                            val newEnd = end.plusDays(30).toString()
                            endDisplay = DateMaskUtils.convertDbDateToDisplayDate(newEnd)
                        } catch (e: Exception) {}
                    }
                }
            ) {
                Text("+30 dias (Próxima consulta)", color = VibrantBlue, fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = Color(0xFF333339), modifier = Modifier.padding(vertical = 10.dp))

        // Seção Refeições do Plano
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Refeições do Plano (${templates.size})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )

            TextButton(onClick = {
                templateBeingEdited = null
                showTemplateDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = VibrantBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Refeição", color = VibrantBlue, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de refeições (Garante sincronização imediata caso uma refeição seja removida!)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { templ ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26262B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${templ.mealName}  (${templ.schedule})",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            if (templ.suggestedFoods.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sugerido: ${templ.suggestedFoods}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            if (templ.substitutions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Substituição: ${templ.substitutions}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                templateBeingEdited = templ
                                showTemplateDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                templates.remove(templ)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Botões de Ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onBack) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val startDb = DateMaskUtils.convertDisplayDateToDbDate(startDisplay)
                    val endDb = DateMaskUtils.convertDisplayDateToDbDate(endDisplay)
                    if (title.isNotBlank() && startDb != null && endDb != null) {
                        onSave(modelId, title.trim(), startDb, endDb, templates.toList())
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
            ) {
                Text("Salvar Plano Alimentar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showTemplateDialog) {
        MealTemplateEditDialog(
            template = templateBeingEdited,
            onDismiss = { showTemplateDialog = false },
            onSave = { updatedTemplate ->
                if (templateBeingEdited != null) {
                    val index = templates.indexOfFirst { it == templateBeingEdited }
                    if (index >= 0) {
                        templates[index] = updatedTemplate
                    }
                } else {
                    templates.add(updatedTemplate)
                }
                showTemplateDialog = false
            }
        )
    }
}

@Composable
fun MealTemplateEditDialog(
    template: MealTemplate?,
    onDismiss: () -> Unit,
    onSave: (MealTemplate) -> Unit
) {
    var mealName by remember { mutableStateOf(template?.mealName ?: "") }
    var schedule by remember { mutableStateOf(template?.schedule ?: "") }
    var suggestedFoods by remember { mutableStateOf(template?.suggestedFoods ?: "") }
    var substitutions by remember { mutableStateOf(template?.substitutions ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF202025),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (template != null) "Editar Refeição" else "Adicionar Refeição",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Nome da Refeição (Sentence capitalization)
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Nome da Refeição", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Café da manhã", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horário planejado
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Horário Previsto", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: 08:00 ou Entre 11 e 13 horas", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Alimentos Sugeridos (Sentence capitalization)
                OutlinedTextField(
                    value = suggestedFoods,
                    onValueChange = { suggestedFoods = it },
                    label = { Text("Alimentos Sugeridos", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Café com leite + Cuscuz com ovos...", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Substituições (Sentence capitalization)
                OutlinedTextField(
                    value = substitutions,
                    onValueChange = { substitutions = it },
                    label = { Text("Substituições", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Ou repetir café da manhã...", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (mealName.isNotBlank()) {
                                onSave(
                                    MealTemplate(
                                        id = template?.id ?: 0,
                                        modelId = template?.modelId ?: 0,
                                        mealName = mealName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                        schedule = schedule.trim(),
                                        suggestedFoods = suggestedFoods.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                        substitutions = substitutions.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                    ) {
                        Text("Salvar Refeição", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
