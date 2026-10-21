package com.dieta.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportDialog(
    initialStartDate: LocalDate = LocalDate.now().minusDays(7),
    initialEndDate: LocalDate = LocalDate.now(),
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (startDate: String, endDate: String) -> Unit
) {
    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var startLocalDate by remember { mutableStateOf(initialStartDate) }
    var endLocalDate by remember { mutableStateOf(initialEndDate) }

    var startDateInput by remember { mutableStateOf(startLocalDate.format(dbFormatter)) }
    var endDateInput by remember { mutableStateOf(endLocalDate.format(dbFormatter)) }

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
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Exportar Relatório PDF",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecione o intervalo de datas para gerar o relatório com os dados do paciente e as refeições realizadas.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                Text(
                    text = "Atalhos rápidos:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            val today = LocalDate.now()
                            startLocalDate = today
                            endLocalDate = today
                            startDateInput = today.format(dbFormatter)
                            endDateInput = today.format(dbFormatter)
                        },
                        label = { Text("Hoje", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF26262B),
                            labelColor = Color.White
                        )
                    )

                    SuggestionChip(
                        onClick = {
                            val today = LocalDate.now()
                            val past7 = today.minusDays(7)
                            startLocalDate = past7
                            endLocalDate = today
                            startDateInput = past7.format(dbFormatter)
                            endDateInput = today.format(dbFormatter)
                        },
                        label = { Text("Últimos 7 dias", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF26262B),
                            labelColor = Color.White
                        )
                    )

                    SuggestionChip(
                        onClick = {
                            val today = LocalDate.now()
                            val past30 = today.minusDays(30)
                            startLocalDate = past30
                            endLocalDate = today
                            startDateInput = past30.format(dbFormatter)
                            endDateInput = today.format(dbFormatter)
                        },
                        label = { Text("Últimos 30 dias", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF26262B),
                            labelColor = Color.White
                        )
                    )

                    SuggestionChip(
                        onClick = {
                            val today = LocalDate.now()
                            val firstDay = today.withDayOfMonth(1)
                            val lastDay = today.withDayOfMonth(today.lengthOfMonth())
                            startLocalDate = firstDay
                            endLocalDate = lastDay
                            startDateInput = firstDay.format(dbFormatter)
                            endDateInput = lastDay.format(dbFormatter)
                        },
                        label = { Text("Mês Atual", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF26262B),
                            labelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Date Input
                OutlinedTextField(
                    value = startDateInput,
                    onValueChange = { startDateInput = it },
                    label = { Text("Data Inicial (AAAA-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("2026-10-15", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = VibrantBlue)
                    },
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

                // End Date Input
                OutlinedTextField(
                    value = endDateInput,
                    onValueChange = { endDateInput = it },
                    label = { Text("Data Final (AAAA-MM-DD)", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("2026-11-14", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = VibrantBlue)
                    },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isGenerating
                    ) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (startDateInput.isNotBlank() && endDateInput.isNotBlank()) {
                                onGenerate(startDateInput.trim(), endDateInput.trim())
                            }
                        },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gerando PDF...")
                        } else {
                            Text("Gerar PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
