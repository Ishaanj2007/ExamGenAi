package com.example.data.repository

import com.example.data.database.QuestionPaperDao
import com.example.data.model.QuestionPaperEntity
import kotlinx.coroutines.flow.Flow

class QuestionPaperRepository(private val dao: QuestionPaperDao) {
    val allQuestionPapers: Flow<List<QuestionPaperEntity>> = dao.getAllQuestionPapers()

    suspend fun getQuestionPaperById(id: Long): QuestionPaperEntity? {
        return dao.getQuestionPaperById(id)
    }

    suspend fun insertQuestionPaper(paper: QuestionPaperEntity): Long {
        return dao.insertQuestionPaper(paper)
    }

    suspend fun updateQuestionPaper(paper: QuestionPaperEntity) {
        dao.updateQuestionPaper(paper)
    }

    suspend fun deleteQuestionPaper(paper: QuestionPaperEntity) {
        dao.deleteQuestionPaper(paper)
    }
}
