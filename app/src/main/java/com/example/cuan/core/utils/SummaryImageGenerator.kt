package com.example.cuan.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility to generate a beautiful, shareable summary card image representing
 * the user's monthly financial report.
 */
object SummaryImageGenerator {

    fun generateAndGetUri(context: Context, transactions: List<Transaction>): Uri? {
        try {
            // 1. Create a bitmap (800 x 800 px)
            val width = 800
            val height = 800
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // 2. Setup colors from the design system
            val colorBackground = 0xFFFCFBF4.toInt() // `#FCFBF4`
            val colorSecondary = 0xFF84A98C.toInt()  // `#84A98C`
            val colorAccent = 0xFFE76F51.toInt()     // `#E76F51`
            val colorOnBackground = 0xFF2C2C2C.toInt() // `#2C2C2C`
            val colorVariant = 0xFFF0EEE4.toInt()    // `#F0EEE4`
            val colorIncome = 0xFF52A675.toInt()     // `#52A675`
            val colorTextSecondary = 0xFF6B7280.toInt() // `#6B7280`

            // 3. Clear canvas with background
            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            paint.color = colorBackground
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Draw a stylish border / inner card frame
            paint.color = colorVariant
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 12f
            canvas.drawRoundRect(24f, 24f, width.toFloat() - 24f, height.toFloat() - 24f, 24f, 24f, paint)
            
            // Draw card header block
            paint.style = Paint.Style.FILL
            paint.color = colorSecondary
            canvas.drawRoundRect(40f, 40f, width.toFloat() - 40f, 130f, 16f, 16f, paint)

            // 4. Draw Header Text
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = 0xFFFFFFFF.toInt() // White text on header
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("CuOne - Ringkasan Keuangan", 60f, 95f, textPaint)

            // 5. Draw Date subtitle
            val currentMonthStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")))
            textPaint.apply {
                color = colorOnBackground
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("Laporan Keuangan: $currentMonthStr", 60f, 180f, textPaint)

            // 6. Calculate summary data
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            // Draw numeric summaries (Income, Expense)
            // Income
            paint.color = colorVariant
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(60f, 210f, 380f, 290f, 12f, 12f, paint)
            
            textPaint.apply {
                color = colorTextSecondary
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("Pemasukan", 80f, 240f, textPaint)
            
            textPaint.apply {
                color = colorIncome
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(CurrencyUtils.formatRupiah(totalIncome), 80f, 275f, textPaint)

            // Expense
            paint.color = colorVariant
            canvas.drawRoundRect(420f, 210f, 740f, 290f, 12f, 12f, paint)
            
            textPaint.apply {
                color = colorTextSecondary
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("Pengeluaran", 440f, 240f, textPaint)
            
            textPaint.apply {
                color = colorAccent
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(CurrencyUtils.formatRupiah(totalExpense), 440f, 275f, textPaint)

            // 7. Draw Donut Chart in the center
            val centerX = 400f
            val centerY = 500f
            val radius = 140f
            val strokeWidth = 50f
            val rectF = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth

            val totalAmount = totalIncome + totalExpense
            if (totalAmount == 0L) {
                // If no transactions, draw a neutral gray chart
                paint.color = 0xFFD1D5DB.toInt()
                canvas.drawCircle(centerX, centerY, radius, paint)
            } else {
                val incomeAngle = (totalIncome.toFloat() / totalAmount.toFloat()) * 360f
                val expenseAngle = (totalExpense.toFloat() / totalAmount.toFloat()) * 360f

                // Draw Income segment (Green)
                paint.color = colorIncome
                canvas.drawArc(rectF, -90f, incomeAngle, false, paint)

                // Draw Expense segment (Orange/Red)
                paint.color = colorAccent
                canvas.drawArc(rectF, -90f + incomeAngle, expenseAngle, false, paint)
            }

            // Draw center donut hole overlay
            paint.style = Paint.Style.FILL
            paint.color = colorBackground
            canvas.drawCircle(centerX, centerY, radius - (strokeWidth / 2f) + 1f, paint)

            // Draw net balance inside donut hole
            textPaint.apply {
                color = colorTextSecondary
                textSize = 14f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText("Sisa Saldo", centerX, centerY - 15f, textPaint)

            textPaint.apply {
                color = colorOnBackground
                textSize = 22f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(CurrencyUtils.formatRupiah(balance), centerX, centerY + 20f, textPaint)

            // 8. Reset align & Draw footer note
            textPaint.apply {
                color = colorTextSecondary
                textSize = 14f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("Dibuat otomatis oleh aplikasi CuOne", centerX, 710f, textPaint)

            // 9. Save image to cache directory
            val sharedImagesDir = File(context.cacheDir, "shared_images")
            if (!sharedImagesDir.exists()) {
                sharedImagesDir.mkdirs()
            }
            val file = File(sharedImagesDir, "cuan_ringkasan.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // 10. Generate content Uri
            val authority = "${context.packageName}.fileprovider"
            return FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
