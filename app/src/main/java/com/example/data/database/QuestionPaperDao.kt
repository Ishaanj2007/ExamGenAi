package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QuestionPaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionPaperDao {
    @Query("SELECT * FROM question_papers ORDER BY createdAt DESC")
    fun getAllQuestionPapers(): Flow<List<QuestionPaperEntity>>

    @Query("SELECT * FROM question_papers WHERE id = :id")
    suspend fun getQuestionPaperById(id: Long): QuestionPaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionPaper(paper: QuestionPaperEntity): Long

    @Update
    suspend fun updateQuestionPaper(paper: QuestionPaperEntity)

    @Delete
    suspend fun deleteQuestionPaper(paper: QuestionPaperEntity)
}
