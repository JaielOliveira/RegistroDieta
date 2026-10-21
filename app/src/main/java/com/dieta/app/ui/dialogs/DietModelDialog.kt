package com.dieta.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dieta.app.data.model.DietModel
import com.dieta.app.data.model.MealTemplate
import com.dieta.app.ui.theme.DarkBackground
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue

@Composable
fun DietModelManagementDialog(
    models: List<DietModel>,
    currentDateDbStr: String,
    onDismiss: () -> Unit,
    onSaveModelWithTemplates: (modelId: Long, title: String, startDate: String, endDate: String, templates: List<MealTemplate>) -> Unit,
    onUpdateModelDates: (modelId: Long, title: String, startDate: String, endDate: String) -> Unit,
    onDeleteModel: (DietModel) -> Unit,
    loadTemplatesForModel: suspend (Long) -> List<MealTemplate>
) {
    var editingModel by remember { mutableStateOf<DietModel?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = DarkCardBg
        ) {
            if (editingModel != null || isCreatingNew) {
                // Edit / Create Screen
                ModelEditorScreen(
                    model = editingModel,
                    onBack = {
                        editingModel = null
                        isCreatingNew = false
                    },
                    onSave = { modelId, title, start, end, templates ->
                        onSaveModelWithTemplates(modelId, title, start, end, templates)
                        editingModel = null
                        isCreatingNew = false
                    },
                    loadTemplatesForModel = loadTemplatesForModel
                )
            } else {
                // Models List Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Modelos de Dieta",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 22.sp
                                )
                            )
                            Text(
                                text = "Configure vigências e refeições prescritas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Novo Modelo" Button
                    Button(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criar Novo Modelo de Dieta", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // List of models
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(models) { model ->
                            val isActive = currentDateDbStr >= model.startDate && currentDateDbStr <= model.endDate

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) Color(0xFF1E3A8A) else Color(0xFF26262B)
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = model.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 16.sp
                                                    )
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF10B981))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Vigente Hoje",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Vigência: ${model.startDate} até ${model.endDate}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingModel = model }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = Color.White
                                                )
                                            }
                                            IconButton(onClick = { onDeleteModel(model) }) {
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
    }
}

@Composable
fun ModelEditorScreen(
    model: DietModel?,
    onBack: () -> Unit,
    onSave: (modelId: Long, title: String, startDate: String, endDate: String, templates: List<MealTemplate>) -> Unit,
    loadTemplatesForModel: suspend (Long) -> List<MealTemplate>
) {
    val modelId = model?.id ?: 0L
    var title by remember { mutableStateOf(model?.title ?: "") }
    var startDate by remember { mutableStateOf(model?.startDate ?: "2026-10-15") }
    var endDate by remember { mutableStateOf(model?.endDate ?: "2026-11-14") }

    val templates = remember { mutableStateListOf<MealTemplate>() }

    // Dialog to add or edit a meal template
    var templateBeingEdited by remember { mutableStateOf<MealTemplate?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(modelId) {
        if (modelId != 0L) {
            val list = loadTemplatesForModel(modelId)
            templates.clear()
            templates.addAll(list)
        } else {
            // Default suggested templates if new model
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
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (model != null) "Editar Modelo de Dieta" else "Novo Modelo de Dieta",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Voltar", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nome do Modelo / Dieta", color = Color.White.copy(alpha = 0.7f)) },
            placeholder = { Text("Ex: Dieta de Definição - Fase 1", color = Color.Gray) },
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

        // Dates Row (Permite estender a vigência até a próxima consulta!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Início (AAAA-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                )
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Término (AAAA-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = VibrantBlue,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Extend Buttons (Estender +15 dias, +30 dias)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    try {
                        val end = java.time.LocalDate.parse(endDate)
                        endDate = end.plusDays(15).toString()
                    } catch (e: Exception) {}
                }
            ) {
                Text("+15 dias de vigência", color = VibrantBlue, fontSize = 12.sp)
            }

            TextButton(
                onClick = {
                    try {
                        val end = java.time.LocalDate.parse(endDate)
                        endDate = end.plusDays(30).toString()
                    } catch (e: Exception) {}
                }
            ) {
                Text("+30 dias (Próxima consulta)", color = VibrantBlue, fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = Color(0xFF333339), modifier = Modifier.padding(vertical = 10.dp))

        // Meal Templates Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Refeições (${templates.size})",
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

        // Templates List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { templ ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26262B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${templ.mealName} - ${templ.schedule}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (templ.suggestedFoods.isNotBlank()) {
                                Text(
                                    text = "Sugerido: ${templ.suggestedFoods}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            if (templ.substitutions.isNotBlank()) {
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
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                templates.remove(templ)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Save Button
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
                    if (title.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                        onSave(modelId, title, startDate, endDate, templates)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
            ) {
                Text("Salvar Modelo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal to add/edit meal template inside model
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
            ) {
                Text(
                    text = if (template != null) "Editar Refeição" else "Adicionar Refeição",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Nome da Refeição", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Café da manhã", color = Color.Gray) },
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

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Horário / Faixa de Horário", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: 08h00 ou Entre 11 e 13 horas", color = Color.Gray) },
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

                OutlinedTextField(
                    value = suggestedFoods,
                    onValueChange = { suggestedFoods = it },
                    label = { Text("Alimentos Sugeridos", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Café com leite + Cuscuz com ovos...", color = Color.Gray) },
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

                OutlinedTextField(
                    value = substitutions,
                    onValueChange = { substitutions = it },
                    label = { Text("Substituições", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Ou repetir café da manhã...", color = Color.Gray) },
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
                                        mealName = mealName.trim(),
                                        schedule = schedule.trim(),
                                        suggestedFoods = suggestedFoods.trim(),
                                        substitutions = substitutions.trim()
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
