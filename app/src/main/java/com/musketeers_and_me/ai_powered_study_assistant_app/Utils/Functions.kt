package com.musketeers_and_me.ai_powered_study_assistant_app.Utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Canvas
import android.os.Environment
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Functions {
    fun countWords(input: String): String {
        val words = input.trim().split("\\s+".toRegex())
        val wordCount = words.size
        println("Word count: $wordCount")
        return "$wordCount Words"
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun countPoints(conceptList: String): String {
        return conceptList.split("\n")
            .filter { it.trim().matches(Regex("^\\d+\\.\\s*.*")) }
            .count().toString() + " Points"
    }

    fun saveTextAsPdf(context: Context, text: String) {
        val fileName = "document_${System.currentTimeMillis()}"
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f

        val x = 10
        var y = 25

        val lines = text.split("\n")
        for (line in lines) {
            canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
            y += 20
        }

        document.finishPage(page)

        try {
            val fileName = "summary_${System.currentTimeMillis()}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            document.writeTo(FileOutputStream(file))

            Log.d("Functions", "PDF saved to ${file.absolutePath}")
            Toast.makeText(context, "PDF saved to Downloads/${fileName}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }


    fun removeBoldAndItalicStyles(spannable: Spannable): String {
        val styleSpans = spannable.getSpans(0, spannable.length, StyleSpan::class.java)
        for (span in styleSpans) {
            spannable.removeSpan(span)
        }
        return spannable.toString()
    }

    fun getHtmlFromEditText(text: Spannable): String {
        return Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
    }

    // Restore from HTML (e.g., from DB)
    fun setHtmlToEditText(text: String): Spanned? {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    }

}