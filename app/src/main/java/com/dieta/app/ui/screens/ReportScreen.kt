package com.dieta.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieta.app.pdf.MealReportEntry
import com.dieta.app.ui.theme.DarkBackground
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import com.dieta.app.ui.viewmodel.AppScreen
import com.dieta.app.ui.viewmodel.DietViewModel
import com.dieta.app.util.DateMaskUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    viewModel: DietViewModel
) {
    val context = LocalContext.current
    val patient by viewModel.patient.collectAsState()
    val reportEntries by viewModel.reportEntries.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()

    val initialStartDb by viewModel.reportStartDate.collectAsState()
    val initialEndDb by viewModel.reportEndDate.collectAsState()

    var startDisplay by remember(initialStartDb) {
        mutableStateOf(DateMaskUtils.convertDbDateToDisplayDate(initialStartDb))
    }
    var endDisplay by remember(initialEndDb) {
        mutableStateOf(DateMaskUtils.convertDbDateToDisplayDate(initialEndDb))
    }

    val dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Relatório de Consumo",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val startDb = DateMaskUtils.convertDisplayDateToDbDate(startDisplay)
                            val endDb = DateMaskUtils.convertDisplayDateToDbDate(endDisplay)
                            if (startDb != null && endDb != null) {
                                viewModel.generateAndSharePdfReport(
                                    context = context,
                                    startDate = startDb,
                                    endDate = endDb,
                                    onSuccess = {
                                        Toast.makeText(context, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Informe datas válidas no formato DD/MM/AAAA", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isGeneratingPdf,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Seletor de Período e Atalhos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intervalo do Relatório (DD/MM/AAAA):",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Data Inicial com máscara automática DD/MM/AAAA
                        OutlinedTextField(
                            value = startDisplay,
                            onValueChange = {
                                val masked = DateMaskUtils.formatDateInput(it)
                                startDisplay = masked
                                DateMaskUtils.convertDisplayDateToDbDate(masked)?.let { validDb ->
                                    DateMaskUtils.convertDisplayDateToDbDate(endDisplay)?.let { validEndDb ->
                                        viewModel.setReportDates(validDb, validEndDb)
                                    }
                                }
                            },
                            label = { Text("Data Inicial", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                            placeholder = { Text("DD/MM/AAAA", color = Color.Gray) },
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

                        // Data Final com máscara automática DD/MM/AAAA
                        OutlinedTextField(
                            value = endDisplay,
                            onValueChange = {
                                val masked = DateMaskUtils.formatDateInput(it)
                                endDisplay = masked
                                DateMaskUtils.convertDisplayDateToDbDate(masked)?.let { validEndDb ->
                                    DateMaskUtils.convertDisplayDateToDbDate(startDisplay)?.let { validStartDb ->
                                        viewModel.setReportDates(validStartDb, validEndDb)
                                    }
                                }
                            },
                            label = { Text("Data Final", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                            placeholder = { Text("DD/MM/AAAA", color = Color.Gray) },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Atalhos rápidos
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                val today = LocalDate.now()
                                val todayDb = today.format(dbFormatter)
                                startDisplay = DateMaskUtils.convertDbDateToDisplayDate(todayDb)
                                endDisplay = startDisplay
                                viewModel.setReportDates(todayDb, todayDb)
                            },
                            label = { Text("Hoje", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF26262B),
                                labelColor = Color.White
                            )
                        )

                        SuggestionChip(
                            onClick = {
                                val today = LocalDate.now()
                                val start = today.minusDays(7).format(dbFormatter)
                                val end = today.format(dbFormatter)
                                startDisplay = DateMaskUtils.convertDbDateToDisplayDate(start)
                                endDisplay = DateMaskUtils.convertDbDateToDisplayDate(end)
                                viewModel.setReportDates(start, end)
                            },
                            label = { Text("Últimos 7 dias", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF26262B),
                                labelColor = Color.White
                            )
                        )

                        SuggestionChip(
                            onClick = {
                                val today = LocalDate.now()
                                val start = today.minusDays(30).format(dbFormatter)
                                val end = today.format(dbFormatter)
                                startDisplay = DateMaskUtils.convertDbDateToDisplayDate(start)
                                endDisplay = DateMaskUtils.convertDbDateToDisplayDate(end)
                                viewModel.setReportDates(start, end)
                            },
                            label = { Text("Últimos 30 dias", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF26262B),
                                labelColor = Color.White
                            )
                        )

                        SuggestionChip(
                            onClick = {
                                val today = LocalDate.now()
                                val first = today.withDayOfMonth(1).format(dbFormatter)
                                val last = today.withDayOfMonth(today.lengthOfMonth()).format(dbFormatter)
                                startDisplay = DateMaskUtils.convertDbDateToDisplayDate(first)
                                endDisplay = DateMaskUtils.convertDbDateToDisplayDate(last)
                                viewModel.setReportDates(first, last)
                            },
                            label = { Text("Mês Atual", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF26262B),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Barra de Estatísticas do Período
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Refeições Realizadas: ${reportEntries.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (patient != null) {
                    Text(
                        text = "Paciente: ${patient!!.name}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            // Visualização Completa Dia a Dia (Scrollbar nativa)
            if (reportEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma refeição registrada no período selecionado.\nAjuste o filtro acima para visualizar outros dias.",
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            } else {
                val groupedByDate = reportEntries.groupBy { it.date }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for ((date, dayEntries) in groupedByDate) {
                        item(key = "header_$date") {
                            // Cabeçalho do dia
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E3A8A).copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = DateMaskUtils.formatDateWithWeekday(date),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        items(
                            items = dayEntries,
                            key = { "entry_${it.date}_${it.mealName}_${it.consumedTime}_${it.hashCode()}" }
                        ) { entry ->
                            ReportMealCard(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMealCard(entry: MealReportEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.mealName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    if (entry.isExtraMeal) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Avulsa",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (entry.consumedTime.isNotBlank()) "Às ${entry.consumedTime}" else "Realizada",
                        color = Color(0xFF4ADE80),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Horário planejado
            if (!entry.isExtraMeal && entry.plannedSchedule.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Planejado: ${entry.plannedSchedule}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alimento consumido
            Text(
                text = "Consumido:",
                fontWeight = FontWeight.SemiBold,
                color = VibrantBlue,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.foodEaten,
                color = Color.White,
                fontSize = 14.sp
            )

            // Alimento prescrito para comparação
            if (!entry.isExtraMeal && entry.suggested.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prescrito no plano: ${entry.suggested}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
