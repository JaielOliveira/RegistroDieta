package com.dieta.app.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dieta.app.data.model.Patient
import com.dieta.app.ui.theme.DarkCardBg
import com.dieta.app.ui.theme.VibrantBlue
import java.util.Locale

@Composable
fun PatientDialog(
    patient: Patient?,
    onDismiss: () -> Unit,
    onSave: (name: String, age: Int, height: Float, initialWeight: Float) -> Unit
) {
    var name by remember { mutableStateOf(patient?.name ?: "") }
    var ageStr by remember { mutableStateOf(patient?.age?.takeIf { it > 0 }?.toString() ?: "") }
    var heightStr by remember { mutableStateOf(patient?.height?.takeIf { it > 0 }?.toString() ?: "") }
    var weightStr by remember { mutableStateOf(patient?.initialWeight?.takeIf { it > 0 }?.toString() ?: "") }

    var nameError by remember { mutableStateOf(false) }

    val heightVal = heightStr.replace(",", ".").toFloatOrNull() ?: 0f
    val normalizedHeight = if (heightVal > 3f) heightVal / 100f else heightVal // Handle 175 -> 1.75
    val weightVal = weightStr.replace(",", ".").toFloatOrNull() ?: 0f

    val imc = if (normalizedHeight > 0f && weightVal > 0f) {
        weightVal / (normalizedHeight * normalizedHeight)
    } else null

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
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Dados do Paciente",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                // IMC Badge preview
                if (imc != null && imc > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF26262B))
                            .padding(12.dp)
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
                                fontSize = 13.sp
                            )
                            Text(
                                text = String.format(Locale("pt", "BR"), "%.1f (%s)", imc, classification),
                                color = VibrantBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                val age = ageStr.toIntOrNull() ?: 0
                                onSave(name, age, normalizedHeight, weightVal)
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
