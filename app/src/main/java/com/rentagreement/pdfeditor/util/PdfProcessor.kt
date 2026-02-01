package com.rentagreement.pdfeditor.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.rentagreement.pdfeditor.domain.model.ExtractedFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfProcessor(private val context: Context) {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun extractFieldsFromPdf(uri: Uri): ExtractedFields = withContext(Dispatchers.IO) {
        try {
            val text = extractTextFromPdf(uri)
            parseFieldsFromText(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting fields from PDF", e)
            ExtractedFields()
        }
    }
    
    private suspend fun extractTextFromPdf(uri: Uri): String {
        val contentResolver = context.contentResolver
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("Cannot open PDF file")
        
        return try {
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val textBuilder = StringBuilder()
            
            for (i in 0 until minOf(pdfRenderer.pageCount, 5)) {
                val page = pdfRenderer.openPage(i)
                
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                val pageText = performOcr(bitmap)
                textBuilder.append(pageText).append("\n")
                
                bitmap.recycle()
                page.close()
            }
            
            pdfRenderer.close()
            parcelFileDescriptor.close()
            
            textBuilder.toString()
        } catch (e: Exception) {
            parcelFileDescriptor.close()
            throw e
        }
    }
    
    private suspend fun performOcr(bitmap: Bitmap): String = withContext(Dispatchers.Main) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed", e)
            ""
        }
    }
    
    private fun parseFieldsFromText(text: String): ExtractedFields {
        val lines = text.lines().map { it.trim() }
        
        val ownerName = extractPattern(
            lines,
            listOf("owner", "landlord", "lessor", "first party"),
            listOf("tenant", "lessee", "second party")
        )
        
        val tenantName = extractPattern(
            lines,
            listOf("tenant", "lessee", "second party"),
            listOf("address", "property", "premises")
        )
        
        val propertyAddress = extractPattern(
            lines,
            listOf("property", "premises", "address", "situated"),
            listOf("rent", "monthly", "amount")
        )
        
        val rentAmount = extractAmount(lines, listOf("rent", "monthly rent", "rental"))
        val depositAmount = extractAmount(lines, listOf("deposit", "security", "advance"))
        val maintenanceAmount = extractAmount(lines, listOf("maintenance", "charges"))
        
        val agreementStartDate = extractDate(lines)
        val durationMonths = extractNumber(lines, listOf("duration", "period", "months", "term"))
        val noticePeriod = extractNumber(lines, listOf("notice", "notice period"))
        
        return ExtractedFields(
            ownerName = ownerName,
            tenantName = tenantName,
            propertyAddress = propertyAddress,
            rentAmount = rentAmount,
            depositAmount = depositAmount,
            agreementStartDate = agreementStartDate,
            durationMonths = durationMonths,
            noticePeriod = noticePeriod,
            maintenanceAmount = maintenanceAmount
        )
    }
    
    private fun extractPattern(
        lines: List<String>,
        keywords: List<String>,
        stopKeywords: List<String> = emptyList()
    ): String {
        val result = StringBuilder()
        var capturing = false
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            
            if (!capturing && keywords.any { lowerLine.contains(it) }) {
                capturing = true
                val parts = line.split(":", "-", "–")
                if (parts.size > 1) {
                    result.append(parts.drop(1).joinToString(" ").trim())
                }
                continue
            }
            
            if (capturing) {
                if (stopKeywords.any { lowerLine.contains(it) } || line.isEmpty()) {
                    break
                }
                if (result.isNotEmpty()) result.append(" ")
                result.append(line)
            }
        }
        
        return result.toString().take(200).trim()
    }
    
    private fun extractAmount(lines: List<String>, keywords: List<String>): String {
        for (line in lines) {
            val lowerLine = line.lowercase()
            if (keywords.any { lowerLine.contains(it) }) {
                val amountRegex = """₹?\s*(\d{1,3}(?:,\d{3})*(?:\.\d{2})?)""".toRegex()
                val match = amountRegex.find(line)
                if (match != null) {
                    return match.groupValues[1].replace(",", "")
                }
            }
        }
        return ""
    }
    
    private fun extractDate(lines: List<String>): String {
        val dateRegex = """(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})|(\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})""".toRegex(RegexOption.IGNORE_CASE)
        
        for (line in lines) {
            if (line.lowercase().contains("date") || line.lowercase().contains("from")) {
                val match = dateRegex.find(line)
                if (match != null) {
                    return match.value
                }
            }
        }
        return ""
    }
    
    private fun extractNumber(lines: List<String>, keywords: List<String>): String {
        for (line in lines) {
            val lowerLine = line.lowercase()
            if (keywords.any { lowerLine.contains(it) }) {
                val numberRegex = """(\d+)""".toRegex()
                val match = numberRegex.find(line)
                if (match != null) {
                    return match.value
                }
            }
        }
        return ""
    }
    
    suspend fun generateSummaryPdf(fields: ExtractedFields, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = buildSummaryContent(fields)
            
            FileOutputStream(outputFile).use { fos ->
                fos.write(content.toByteArray())
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF", e)
            false
        }
    }
    
    private fun buildSummaryContent(fields: ExtractedFields): String {
        return """
            |RENT AGREEMENT SUMMARY
            |Generated on: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}
            |
            |============================================
            |
            |PARTIES INVOLVED:
            |• Owner/Landlord: ${fields.ownerName.ifEmpty { "Not specified" }}
            |• Tenant/Lessee: ${fields.tenantName.ifEmpty { "Not specified" }}
            |
            |PROPERTY DETAILS:
            |• Address: ${fields.propertyAddress.ifEmpty { "Not specified" }}
            |
            |FINANCIAL TERMS:
            |• Monthly Rent: ₹${fields.rentAmount.ifEmpty { "Not specified" }}
            |• Security Deposit: ₹${fields.depositAmount.ifEmpty { "Not specified" }}
            |• Maintenance Amount: ₹${fields.maintenanceAmount.ifEmpty { "Not specified" }}
            |
            |AGREEMENT TERMS:
            |• Start Date: ${fields.agreementStartDate.ifEmpty { "Not specified" }}
            |• Duration: ${fields.durationMonths.ifEmpty { "Not specified" }} months
            |• Notice Period: ${fields.noticePeriod.ifEmpty { "Not specified" }} days
            |
            |KEY CLAUSES:
            |• The tenant shall pay rent on or before 5th of every month
            |• The tenant shall maintain the property in good condition
            |• The tenant shall not sublet without owner's written permission
            |• Either party may terminate with proper notice period
            |• Security deposit shall be refunded after deducting dues
            |
            |============================================
            |
            |This is a computer-generated summary for quick reference.
            |Please refer to the original agreement for complete terms.
        """.trimMargin()
    }
    
    companion object {
        private const val TAG = "PdfProcessor"
    }
}
