package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "question_papers")
data class QuestionPaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val difficulty: String,
    val totalMarks: Int,
    val timeAllowedMinutes: Int,
    val materialText: String,
    val questionsJson: String, // Moshi serialized List<Question>
    val answerKeyJson: String?, // Moshi serialized List<AnswerKeyItem> or answer text
    val institutionName: String = "",
    val classOrExamName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Question(
    val id: String,
    val type: String, // "MCQ", "SHORT", "LONG", "OBJECTIVE"
    val questionText: String,
    val options: List<String>? = null, // only for MCQ
    val correctAnswer: String? = null, // for answer key
    val marks: Int
)

@JsonClass(generateAdapter = true)
data class QuestionListWrapper(
    val questions: List<Question>
)
