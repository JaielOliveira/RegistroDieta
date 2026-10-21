package com.dieta.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateMaskUtils {

    private val dbDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Aplica máscara automática DD/MM/AAAA conforme o usuário digita apenas números.
     */
    fun formatDateInput(input: String): String {
        val digits = input.filter { it.isDigit() }.take(8)
        return when {
            digits.length <= 2 -> digits
            digits.length <= 4 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
            else -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
        }
    }

    /**
     * Aplica máscara de altura com vírgula (ex: 1,75 a partir de dígitos '175').
     */
    fun formatHeightInput(input: String): String {
        val digits = input.filter { it.isDigit() }.take(3)
        return when {
            digits.isEmpty() -> ""
            digits.length == 1 -> digits
            else -> "${digits[0]},${digits.substring(1)}"
        }
    }

    /**
     * Aplica máscara de peso com vírgula para separar quilos de gramas (ex: '70,5' ou '115,2').
     */
    fun formatWeightInput(input: String): String {
        val clean = input.replace('.', ',')
        val parts = clean.split(',')
        if (parts.size > 1) {
            val intPart = parts[0].filter { it.isDigit() }.take(3)
            val decPart = parts[1].filter { it.isDigit() }.take(1)
            return if (decPart.isNotEmpty()) "$intPart,$decPart" else "$intPart,"
        }
        val digits = input.filter { it.isDigit() }.take(4)
        return when {
            digits.length <= 2 -> digits
            digits.length == 3 -> "${digits.substring(0, 2)},${digits.substring(2)}"
            else -> "${digits.substring(0, 3)},${digits.substring(3)}"
        }
    }

    fun parseHeight(formatted: String): Float? {
        val normalized = formatted.replace(',', '.').trim()
        val num = normalized.toFloatOrNull() ?: return null
        return if (num in 0.50f..2.50f) num else null
    }

    fun parseWeight(formatted: String): Float? {
        val normalized = formatted.replace(',', '.').trim()
        val num = normalized.toFloatOrNull() ?: return null
        return if (num in 20.0f..350.0f) num else null
    }

    /**
     * Aplica máscara automática HH:mm conforme o usuário digita horários.
     */
    fun formatTimeInput(input: String): String {
        val digits = input.filter { it.isDigit() }.take(4)
        return when {
            digits.length <= 2 -> digits
            else -> "${digits.substring(0, 2)}:${digits.substring(2)}"
        }
    }

    /**
     * Converte DD/MM/AAAA para YYYY-MM-DD para salvar no banco de dados.
     */
    fun convertDisplayDateToDbDate(displayDate: String): String? {
        val clean = displayDate.trim()
        if (clean.length != 10) return null
        return try {
            val date = LocalDate.parse(clean, displayDateFormatter)
            date.format(dbDateFormatter)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converte YYYY-MM-DD para DD/MM/AAAA para exibição amigável.
     */
    fun convertDbDateToDisplayDate(dbDate: String): String {
        val clean = dbDate.trim()
        return try {
            val date = LocalDate.parse(clean, dbDateFormatter)
            date.format(displayDateFormatter)
        } catch (e: Exception) {
            clean
        }
    }

    /**
     * Obtém o horário atual no formato HH:mm.
     */
    fun getCurrentTime(): String {
        return LocalTime.now().format(timeFormatter)
    }

    /**
     * Formata data por extenso para relatórios e títulos.
     */
    fun formatDateWithWeekday(dbDate: String): String {
        return try {
            val date = LocalDate.parse(dbDate, dbDateFormatter)
            val dtf = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
            val formatted = date.format(dtf)
            formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
        } catch (e: Exception) {
            dbDate
        }
    }
}
