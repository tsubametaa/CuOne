package com.example.cuan.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

// Utility to export transactions to a PDF file in the Downloads folder.
 
object PdfGenerator {

    fun export(context: Context, transactions: List<Transaction>) {
        try {
            val pdfDocument = PdfDocument()
            
            // Standard A4 dimensions: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
            }
            
            val titlePaint = Paint().apply {
                color = Color.rgb(132, 169, 140) // `#84A98C` (Secondary)
                textSize = 22f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(44, 44, 44) // `#2C2C2C` (OnBackground)
                textSize = 11f
                isFakeBoldText = true
            }
            
            // Draw Title Header
            canvas.drawText("Laporan Keuangan CuOne", 40f, 60f, titlePaint)
            
            // Draw Subtitle / Export Date
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            val dateStr = java.time.LocalDate.now().format(formatter)
            canvas.drawText("Tanggal Cetak: $dateStr", 40f, 85f, textPaint)
            
            // Calculate totals
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = totalIncome - totalExpense
            
            // Draw a beautiful summary card
            paint.color = Color.rgb(240, 238, 228) // `#F0EEE4` (BackgroundVariant)
            canvas.drawRoundRect(40f, 105f, 555f, 175f, 8f, 8f, paint)
            
            val subHeaderPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                isFakeBoldText = true
            }
            
            canvas.drawText("Ringkasan Keuangan", 55f, 128f, subHeaderPaint)
            
            val incomeText = "Pemasukan: ${CurrencyUtils.formatRupiah(totalIncome)}"
            val expenseText = "Pengeluaran: ${CurrencyUtils.formatRupiah(totalExpense)}"
            val balanceText = "Saldo Bersih: ${CurrencyUtils.formatRupiah(balance)}"
            
            canvas.drawText(incomeText, 55f, 155f, textPaint.apply { color = Color.rgb(82, 166, 117) }) // IncomeGreen
            canvas.drawText(expenseText, 220f, 155f, textPaint.apply { color = Color.rgb(231, 111, 81) }) // ExpenseRed
            canvas.drawText(balanceText, 385f, 155f, textPaint.apply { color = Color.BLACK })
            
            // Draw Table Headers
            var yPosition = 215f
            canvas.drawText("Tanggal", 40f, yPosition, headerPaint)
            canvas.drawText("Kategori", 140f, yPosition, headerPaint)
            canvas.drawText("Catatan", 240f, yPosition, headerPaint)
            canvas.drawText("Nominal", 460f, yPosition, headerPaint)
            
            // Horizontal line under table header
            paint.color = Color.rgb(220, 220, 220)
            paint.strokeWidth = 1f
            canvas.drawLine(40f, yPosition + 5f, 555f, yPosition + 5f, paint)
            
            yPosition += 25f
            
            // Draw transaction rows
            transactions.forEach { tx ->
                // Start a new page if the limit is exceeded
                if (yPosition > 800f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                
                canvas.drawText(tx.date.toString(), 40f, yPosition, textPaint.apply { color = Color.BLACK })
                canvas.drawText(tx.category, 140f, yPosition, textPaint)
                
                // Truncate note if too long
                val noteDisplay = if (tx.note.length > 28) tx.note.substring(0, 25) + "..." else tx.note
                canvas.drawText(noteDisplay.ifEmpty { "-" }, 240f, yPosition, textPaint)
                
                val amountText = if (tx.type == TransactionType.INCOME) {
                    "+ ${CurrencyUtils.formatRupiah(tx.amount)}"
                } else {
                    "- ${CurrencyUtils.formatRupiah(tx.amount)}"
                }
                
                val amountColor = if (tx.type == TransactionType.INCOME) Color.rgb(82, 166, 117) else Color.rgb(231, 111, 81)
                textPaint.color = amountColor
                canvas.drawText(amountText, 460f, yPosition, textPaint)
                
                yPosition += 20f
            }
            
            pdfDocument.finishPage(page)
            
            // Save file into Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "Laporan_Keuangan_CuOne_${System.currentTimeMillis()}.pdf"
            val file = File(downloadsDir, fileName)
            
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            Toast.makeText(context, "Laporan disimpan ke: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
