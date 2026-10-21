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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dieta.app.ui.theme.DarkBackground
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import com.dieta.app.ui.viewmodel.AppScreen
import com.dieta.app.ui.viewmodel.DietViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(
    viewModel: DietViewModel
) {
    val context = LocalContext.current
    val currentPatient by viewModel.patient.collectAsState()

    var name by remember(currentPatient) { mutableStateOf(currentPatient?.name ?: "") }
    var ageStr by remember(currentPatient) { mutableStateOf(currentPatient?.age?.takeIf { it > 0 }?.toString() ?: "") }
    var heightStr by remember(currentPatient) { mutableStateOf(currentPatient?.height?.takeIf { it > 0 }?.toString() ?: "") }
    var weightStr by remember(currentPatient) { mutableStateOf(currentPatient?.initialWeight?.takeIf { it > 0 }?.toString() ?: "") }

    var nameError by remember { mutableStateOf(false) }

    val heightVal = heightStr.replace(",", ".").toFloatOrNull() ?: 0f
    val normalizedHeight = if (heightVal > 3f) heightVal / 100f else heightVal
    val weightVal = weightStr.replace(",", ".").toFloatOrNull() ?: 0f

    val imc = if (normalizedHeight > 0f && weightVal > 0f) {
        weightVal / (normalizedHeight * normalizedHeight)
    } else null

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dados do Paciente",
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nome
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Nome Completo", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ex: Maria da Silva", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = VibrantBlue)
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                isError = nameError,
                supportingText = {
                    if (nameError) Text("Informe o nome do paciente", color = Color.Red)
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

            // Idade
            OutlinedTextField(
                value = ageStr,
                onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Idade (anos)", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ex: 30", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Today, contentDescription = null, tint = VibrantBlue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Altura
            OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text("Altura (m ou cm)", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ex: 1.75 ou 175", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Height, contentDescription = null, tint = VibrantBlue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

            // Peso Inicial
            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("Peso Inicial (kg)", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Ex: 75.5", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = VibrantBlue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

            // Card Informativo do IMC
            if (imc != null && imc > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCardBg)
                        .padding(16.dp)
                ) {
                    val classification = when {
                        imc < 18.5f -> "Abaixo do peso"
                        imc < 25.0f -> "Peso ideal"
                        imc < 30.0f -> "Sobrepeso"
                        else -> "Obesidade"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IMC Calculado:",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "%.1f (%s)", imc, classification),
                            color = VibrantBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val age = ageStr.toIntOrNull() ?: 0
                        viewModel.savePatient(name, age, normalizedHeight, weightVal)
                        Toast.makeText(context, "Dados do paciente salvos com sucesso!", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.MAIN_MENU)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue)
            ) {
                Text("Salvar Dados do Paciente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
