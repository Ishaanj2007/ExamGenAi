package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.api.RetrofitClient
import com.example.data.model.Question
import com.example.data.model.QuestionPaperEntity
import com.example.data.model.QuestionListWrapper
import com.squareup.moshi.JsonAdapter
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportToPdf(context: Context, paper: QuestionPaperEntity): File {
        val moshi = RetrofitClient.getMoshi()
        val adapter: JsonAdapter<QuestionListWrapper> = moshi.adapter(QuestionListWrapper::class.java)
        
        val questions: List<Question> = try {
            adapter.fromJson(paper.questionsJson)?.questions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var currentY = 54f

        fun startNewPage(isAnswerKey: Boolean = false) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            
            // Draw Running Header
            val headerText = if (isAnswerKey) "ANSWER KEY" else "QUESTION PAPER"
            canvas.drawText(headerText, 54f, 40f, boldBodyPaint)
            val pageNumText = "Page $pageNumber"
            canvas.drawText(pageNumText, 595f - 54f - bodyPaint.measureText(pageNumText), 40f, bodyPaint)
            canvas.drawLine(54f, 45f, 595f - 54f, 45f, linePaint)
            
            currentY = 64f
        }

        // --- PAGE 1: HEADER ---
        // School/Institution Header (Centered, bold, dynamic caps)
        val institutionText = if (paper.institutionName.isNotBlank()) {
            paper.institutionName.uppercase()
        } else {
            "EXAMINATION / EVALUATION PAPER"
        }
        
        val institutionPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val instLayout = StaticLayout.Builder.obtain(
            institutionText, 
            0, 
            institutionText.length, 
            institutionPaint, 
            487
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()
        
        canvas.save()
        canvas.translate(54f, currentY)
        instLayout.draw(canvas)
        canvas.restore()
        currentY += instLayout.height + 4f

        // Class or Exam Title Header
        if (paper.classOrExamName.isNotBlank()) {
            val classExamText = paper.classOrExamName
            val classExamPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val classExamLayout = StaticLayout.Builder.obtain(
                classExamText,
                0,
                classExamText.length,
                classExamPaint,
                487
            ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()
            
            canvas.save()
            canvas.translate(54f, currentY)
            classExamLayout.draw(canvas)
            canvas.restore()
            currentY += classExamLayout.height + 4f
        }

        // Subtopic/Source Title Header
        val subtopicText = paper.title
        val subtopicPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtopicLayout = StaticLayout.Builder.obtain(
            subtopicText,
            0,
            subtopicText.length,
            subtopicPaint,
            487
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()
        
        canvas.save()
        canvas.translate(54f, currentY)
        subtopicLayout.draw(canvas)
        canvas.restore()
        currentY += subtopicLayout.height + 15f

        // Metadata Details Row
        canvas.drawText("SUBJECT: ${paper.subject.uppercase()}", 54f, currentY, boldBodyPaint)
        currentY += 15f

        canvas.drawText("TIME ALLOWED: ${paper.timeAllowedMinutes} MINUTES", 54f, currentY, boldBodyPaint)
        val marksText = "MAXIMUM MARKS: ${paper.totalMarks}"
        canvas.drawText(marksText, 595f - 54f - boldBodyPaint.measureText(marksText), currentY, boldBodyPaint)
        currentY += 10f

        // Divider
        canvas.drawLine(54f, currentY, 595f - 54f, currentY, linePaint)
        currentY += 18f

        // General Instructions Block
        canvas.drawText("General Instructions:", 54f, currentY, boldBodyPaint)
        currentY += 12f
        canvas.drawText("1. All questions are compulsory.", 64f, currentY, bodyPaint)
        currentY += 12f
        canvas.drawText("2. Read each question carefully before writing answers.", 64f, currentY, bodyPaint)
        currentY += 18f

        // --- DRAW QUESTIONS (Categorized into clean sections) ---
        var currentSectionType: String? = null
        
        for ((index, question) in questions.withIndex()) {
            val qType = question.type.uppercase()
            
            // Render section boundary headers
            if (qType != currentSectionType) {
                currentSectionType = qType
                val sectionHeader = when (qType) {
                    "OBJECTIVE" -> "SECTION A: OBJECTIVE QUESTIONS"
                    "MCQ" -> "SECTION B: MULTIPLE CHOICE QUESTIONS (MCQs)"
                    "SHORT" -> "SECTION C: SHORT ANSWER QUESTIONS"
                    "LONG" -> "SECTION D: LONG ANSWER / ESSAY QUESTIONS"
                    else -> "SECTION: $qType QUESTIONS"
                }
                
                val sectLayout = StaticLayout.Builder.obtain(
                    sectionHeader,
                    0,
                    sectionHeader.length,
                    sectionPaint,
                    487
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
                
                if (currentY + sectLayout.height + 30f > 788f) {
                    startNewPage()
                }
                
                canvas.save()
                canvas.translate(54f, currentY)
                sectLayout.draw(canvas)
                canvas.restore()
                
                currentY += sectLayout.height + 4f
                canvas.drawLine(54f, currentY, 54f + sectionPaint.measureText(sectionHeader), currentY, linePaint)
                currentY += 14f
            }

            val qNumText = "Q${index + 1}. "
            val marksSuffix = " [${question.marks} ${if (question.marks > 1) "Marks" else "Mark"}]"
            val qText = "$qNumText${question.questionText}$marksSuffix"

            val qLayout = StaticLayout.Builder.obtain(
                qText, 
                0, 
                qText.length, 
                bodyPaint, 
                487
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            var requiredHeight = qLayout.height + 12f

            // Calculate options height for MCQs
            val optLayouts = mutableListOf<StaticLayout>()
            if (question.type == "MCQ" && !question.options.isNullOrEmpty()) {
                for ((optIdx, option) in question.options.withIndex()) {
                    val prefix = when (optIdx) {
                        0 -> "(A) "
                        1 -> "(B) "
                        2 -> "(C) "
                        3 -> "(D) "
                        else -> "- "
                    }
                    val fullOpt = "$prefix$option"
                    val optLayout = StaticLayout.Builder.obtain(
                        fullOpt, 
                        0, 
                        fullOpt.length, 
                        bodyPaint, 
                        460
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
                    optLayouts.add(optLayout)
                    requiredHeight += optLayout.height + 4f
                }
            }

            // Margin Page check
            if (currentY + requiredHeight > 788f) {
                startNewPage()
            }

            // Draw Question Text
            canvas.save()
            canvas.translate(54f, currentY)
            qLayout.draw(canvas)
            canvas.restore()
            currentY += qLayout.height + 6f

            // Draw option text
            if (question.type == "MCQ" && optLayouts.isNotEmpty()) {
                for (optLayout in optLayouts) {
                    canvas.save()
                    canvas.translate(74f, currentY)
                    optLayout.draw(canvas)
                    canvas.restore()
                    currentY += optLayout.height + 4f
                }
            }

            currentY += 12f
        }

        // --- DRAW ANSWER KEY (On new page) ---
        startNewPage(isAnswerKey = true)

        val akTitleText = "TEACHER'S ANSWER KEY & EVALUATION GUIDE"
        val akTitleLayout = StaticLayout.Builder.obtain(
            akTitleText, 
            0, 
            akTitleText.length, 
            sectionPaint, 
            487
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

        canvas.save()
        canvas.translate(54f, currentY)
        akTitleLayout.draw(canvas)
        canvas.restore()
        currentY += akTitleLayout.height + 15f

        var currentAkSectionType: String? = null
        for ((index, question) in questions.withIndex()) {
            val qType = question.type.uppercase()
            
            // Dynamic section category headers in answer keys too
            if (qType != currentAkSectionType) {
                currentAkSectionType = qType
                val sectionHeader = when (qType) {
                    "OBJECTIVE" -> "SECTION A ANSWERS"
                    "MCQ" -> "SECTION B ANSWERS"
                    "SHORT" -> "SECTION C ANSWERS"
                    "LONG" -> "SECTION D ANSWERS"
                    else -> "$qType ANSWERS"
                }
                
                if (currentY + 30f > 788f) {
                    startNewPage(isAnswerKey = true)
                }
                
                canvas.drawText(sectionHeader, 54f, currentY, boldBodyPaint)
                currentY += 12f
                canvas.drawLine(54f, currentY, 54f + boldBodyPaint.measureText(sectionHeader), currentY, linePaint)
                currentY += 14f
            }

            val keyLabel = "Q${index + 1} Answer Guidelines: "
            val fullKeyText = "$keyLabel${question.correctAnswer ?: "N/A"}"
            
            val akLayout = StaticLayout.Builder.obtain(
                fullKeyText, 
                0, 
                fullKeyText.length, 
                bodyPaint, 
                487
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            if (currentY + akLayout.height + 12f > 788f) {
                startNewPage(isAnswerKey = true)
            }

            canvas.save()
            canvas.translate(54f, currentY)
            akLayout.draw(canvas)
            canvas.restore()
            currentY += akLayout.height + 12f
        }

        document.finishPage(page)

        // Save PDF to cache dir (for sharing/opening)
        val directory = context.cacheDir
        val safeSubject = paper.subject.replace(Regex("[^a-zA-Z0-9]"), "_")
        val file = File(directory, "QuestionPaper_${safeSubject}_${System.currentTimeMillis()}.pdf")
        
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        
        document.close()
        return file
    }
}
