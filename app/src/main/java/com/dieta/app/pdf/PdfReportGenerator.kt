package com.dieta.app.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.dieta.app.data.model.Patient
import com.dieta.app.util.DateMaskUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class MealReportEntry(
    val date: String,
    val mealName: String,
    val plannedSchedule: String,
    val consumedTime: String,
    val suggested: String,
    val foodEaten: String,
    val isExtraMeal: Boolean = false
)

class PdfReportGenerator(private val context: Context) {

    fun generateReport(
        patient: Patient?,
        startDate: String,
        endDate: String,
        entries: List<MealReportEntry>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paints
        val paintPrimary = Paint().apply {
            color = Color.parseColor("#1E52E8")
            isAntiAlias = true
        }

        val paintDark = Paint().apply {
            color = Color.parseColor("#1E1E1E")
            isAntiAlias = true
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val paintDarkBold = Paint().apply {
            color = Color.parseColor("#1E1E1E")
            isAntiAlias = true
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintGray = Paint().apply {
            color = Color.parseColor("#666666")
            isAntiAlias = true
            textSize = 10f
        }

        val paintBoxBg = Paint().apply {
            color = Color.parseColor("#F4F6FB")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintBorder = Paint().apply {
            color = Color.parseColor("#D0D7DE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        var y = margin

        fun drawHeader() {
            // Header Title
            paintPrimary.textSize = 17f
            paintPrimary.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("RELATÓRIO DO PLANO ALIMENTAR E CONSUMO DIÁRIO", margin, y + 16f, paintPrimary)
            y += 24f

            // Generation Date
            val nowStr = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
            canvas.drawText("Gerado em: $nowStr", margin, y + 10f, paintGray)
            y += 18f

            // Horizontal line
            paintPrimary.strokeWidth = 2f
            canvas.drawLine(margin, y, margin + contentWidth, y, paintPrimary)
            y += 16f
        }

        fun drawFooter(curPageNum: Int) {
            val footerPaint = Paint().apply {
                color = Color.parseColor("#888888")
                textSize = 9f
                isAntiAlias = true
            }
            val text = "Página $curPageNum - Registro e Acompanhamento de Dieta Diária"
            val textWidth = footerPaint.measureText(text)
            canvas.drawText(text, (pageWidth - textWidth) / 2, pageHeight - 20f, footerPaint)
        }

        fun checkPageBreak(requiredHeight: Float) {
            if (y + requiredHeight > pageHeight - 50f) {
                drawFooter(pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = margin

                // Running header for subsequent pages
                paintPrimary.textSize = 11f
                paintPrimary.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("RELATÓRIO ALIMENTAR (Continuação)", margin, y + 10f, paintPrimary)
                y += 18f
                paintBorder.strokeWidth = 1f
                canvas.drawLine(margin, y, margin + contentWidth, y, paintBorder)
                y += 14f
            }
        }

        // Draw initial header
        drawHeader()

        // 1. Patient Info Card
        checkPageBreak(90f)
        val boxRect = RectF(margin, y, margin + contentWidth, y + 80f)
        canvas.drawRoundRect(boxRect, 8f, 8f, paintBoxBg)
        canvas.drawRoundRect(boxRect, 8f, 8f, paintBorder)

        val patientName = patient?.name ?: "Paciente Não Cadastrado"
        val patientAge = if (patient != null && patient.age > 0) "${patient.age} anos" else "--"
        val patientHeight = if (patient != null && patient.height > 0) String.format(Locale("pt", "BR"), "%.2f m", patient.height) else "--"
        val patientWeight = if (patient != null && patient.initialWeight > 0) String.format(Locale("pt", "BR"), "%.1f kg", patient.initialWeight) else "--"

        var imcText = "--"
        if (patient != null && patient.height > 0 && patient.initialWeight > 0) {
            val h = patient.height
            val imc = patient.initialWeight / (h * h)
            val imcCategory = when {
                imc < 18.5f -> "Abaixo do peso"
                imc < 25.0f -> "Peso normal"
                imc < 30.0f -> "Sobrepeso"
                else -> "Obesidade"
            }
            imcText = String.format(Locale("pt", "BR"), "%.1f (%s)", imc, imcCategory)
        }

        paintDarkBold.textSize = 12f
        canvas.drawText("DADOS DO PACIENTE", margin + 12f, y + 20f, paintDarkBold)

        paintDark.textSize = 10.5f
        canvas.drawText("Nome: $patientName", margin + 12f, y + 40f, paintDark)
        canvas.drawText("Idade: $patientAge", margin + 260f, y + 40f, paintDark)
        canvas.drawText("Altura: $patientHeight", margin + 12f, y + 60f, paintDark)
        canvas.drawText("Peso Inicial: $patientWeight", margin + 140f, y + 60f, paintDark)
        canvas.drawText("IMC Inicial: $imcText", margin + 260f, y + 60f, paintDark)

        y += 94f

        // 2. Summary of Period
        checkPageBreak(50f)
        val startFormatted = DateMaskUtils.convertDbDateToDisplayDate(startDate)
        val endFormatted = DateMaskUtils.convertDbDateToDisplayDate(endDate)
        paintDarkBold.textSize = 11.5f
        canvas.drawText("Período Consultado: $startFormatted a $endFormatted", margin, y + 10f, paintDarkBold)
        paintGray.textSize = 10f
        canvas.drawText("Total de refeições registradas no período: ${entries.size}", margin, y + 24f, paintGray)
        y += 36f

        // 3. Entries Grouped by Date
        if (entries.isEmpty()) {
            checkPageBreak(40f)
            paintDark.textSize = 11f
            canvas.drawText("Nenhuma refeição registrada no período selecionado.", margin, y + 20f, paintDark)
            y += 40f
        } else {
            val groupedByDate = entries.groupBy { it.date }

            for ((date, dayEntries) in groupedByDate) {
                checkPageBreak(60f)

                // Date separator banner
                val dateRect = RectF(margin, y, margin + contentWidth, y + 24f)
                val paintDateHeader = Paint().apply {
                    color = Color.parseColor("#EBF1FF")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawRoundRect(dateRect, 4f, 4f, paintDateHeader)

                val dateFormatted = DateMaskUtils.formatDateWithWeekday(date)
                paintPrimary.textSize = 11f
                paintPrimary.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(dateFormatted, margin + 8f, y + 16f, paintPrimary)
                y += 30f

                // List day meals
                for (entry in dayEntries) {
                    checkPageBreak(64f)

                    val entryBox = RectF(margin, y, margin + contentWidth, y + 54f)
                    canvas.drawRoundRect(entryBox, 6f, 6f, paintBorder)

                    // Meal title & times
                    paintDarkBold.textSize = 10.5f
                    val timeSchedule = if (entry.isExtraMeal) {
                        "[AVULSA]"
                    } else if (entry.plannedSchedule.isNotBlank()) {
                        "Previsto: ${entry.plannedSchedule}"
                    } else ""

                    canvas.drawText("${entry.mealName}  $timeSchedule", margin + 10f, y + 16f, paintDarkBold)

                    // Consumed checkmark badge
                    val paintCheck = Paint().apply {
                        color = Color.parseColor("#2E7D32")
                        textSize = 9.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    val consumedTag = if (entry.consumedTime.isNotBlank()) "✓ ÀS ${entry.consumedTime}" else "✓ FEITO"
                    canvas.drawText(consumedTag, margin + contentWidth - 85f, y + 16f, paintCheck)

                    // Food consumed
                    paintDark.textSize = 9.5f
                    val foodText = "Consumido: ${entry.foodEaten}"
                    val truncatedFood = if (foodText.length > 85) foodText.take(82) + "..." else foodText
                    canvas.drawText(truncatedFood, margin + 10f, y + 32f, paintDark)

                    // Suggested (prescribed)
                    if (!entry.isExtraMeal && entry.suggested.isNotBlank()) {
                        val suggText = "Plano Sugerido: ${entry.suggested}"
                        val truncatedSugg = if (suggText.length > 90) suggText.take(87) + "..." else suggText
                        canvas.drawText(truncatedSugg, margin + 10f, y + 46f, paintGray)
                    }

                    y += 62f
                }
                y += 8f
            }
        }

        // Draw final page footer
        drawFooter(pageNumber)
        pdfDocument.finishPage(page)

        // Save to file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(reportsDir, "Relatorio_Dieta_$timeStamp.pdf")
        val outputStream = FileOutputStream(pdfFile)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return pdfFile
    }

    fun openOrSharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Abrir Relatório PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório PDF"))
        }
    }
}
