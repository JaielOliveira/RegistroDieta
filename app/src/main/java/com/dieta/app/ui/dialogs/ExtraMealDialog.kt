package com.dieta.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dieta.app.data.model.MealWithStatus
import com.dieta.app.ui.components.AppTimePickerDialog
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import com.dieta.app.util.DateMaskUtils

@Composable
fun ExtraMealDialog(
    existingMeal: MealWithStatus? = null,
    onDismiss: () -> Unit,
    onSave: (mealName: String, foodEaten: String, consumedTime: String, logId: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val existingLog = existingMeal?.dailyLog

    var mealName by remember {
        mutableStateOf(existingLog?.extraMealName ?: existingMeal?.mealName ?: "Lanche Extra")
    }
    var foodEaten by remember { mutableStateOf(existingLog?.foodEaten ?: "") }
    var consumedTime by remember {
        mutableStateOf(existingLog?.consumedTime?.ifBlank { null } ?: DateMaskUtils.getCurrentTime())
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var foodError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkCardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (existingLog != null) "Editar Refeição Avulsa" else "Adicionar Refeição Avulsa",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Esta refeição é registrada apenas para hoje, sem alterar seu plano alimentar.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nome da Refeição Avulsa
                OutlinedTextField(
                    value = mealName,
                    onValueChange = {
                        mealName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nome da refeição", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: Lanche da tarde, Pré-treino, Snack", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = VibrantBlue)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = VibrantBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seletor de Horário de consumo (Abre o TimePicker nativo)
                Text(
                    text = "Horário do Consumo:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF26262B))
                        .clickable { showTimePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = VibrantBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = consumedTime,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Alterar",
                                color = VibrantBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = VibrantBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alimento consumido
                OutlinedTextField(
                    value = foodEaten,
                    onValueChange = {
                        foodEaten = it
                        if (it.isNotBlank()) foodError = false
                    },
                    label = { Text("Alimento consumido", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Ex: 1 barra de proteína e 1 banana", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = foodError,
                    supportingText = {
                        if (foodError) Text("Informe o alimento consumido", color = Color.Red)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VibrantBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = VibrantBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (existingLog != null && onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir refeição avulsa",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = Color.White.copy(alpha = 0.8f))
                        }

                        Button(
                            onClick = {
                                if (mealName.isBlank()) {
                                    nameError = true
                                } else if (foodEaten.isBlank()) {
                                    foodError = true
                                } else {
                                    val formattedName = mealName.trim().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    }
                                    val formattedFood = foodEaten.trim().replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    }
                                    onSave(formattedName, formattedFood, consumedTime.trim(), existingLog?.id ?: 0L)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                        ) {
                            Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialTime = consumedTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { pickedTime ->
                consumedTime = pickedTime
                showTimePicker = false
            }
        )
    }
}
