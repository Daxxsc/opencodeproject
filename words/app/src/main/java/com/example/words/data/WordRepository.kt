package com.example.words.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class WordRepository(private val wordDao: WordDao) {
    
    suspend fun insertWord(word: Word): Long {
        return wordDao.insert(word)
    }
    
    suspend fun updateWord(word: Word) {
        wordDao.update(word)
    }
    
    fun getAllWords(): Flow<List<Word>> = flow {
        emit(wordDao.getAllWords())
    }
    
    suspend fun getRandomUnknownWord(): Word? {
        return wordDao.getRandomUnknownWord()
    }
    
    suspend fun markAsKnown(word: Word) {
        word.status = "KNOWN"
        word.reviewStage = 0
        word.nextReviewDate = null
        word.lastReviewedDate = Date()
        wordDao.update(word)
    }
    
    suspend fun markAsUnknown(word: Word) {
        word.status = "UNKNOWN"
        word.reviewStage = 0
        word.nextReviewDate = null
        word.lastReviewedDate = Date()
        wordDao.update(word)
    }
    
    suspend fun markForReview(word: Word) {
        word.status = "REVIEWING"
        word.reviewStage = 1
        word.lastReviewedDate = Date()
        // 第1次复习：立即（0天后）
        word.nextReviewDate = calculateNextReviewDate(1)
        wordDao.update(word)
    }
    
    suspend fun toggleHidden(word: Word) {
        word.setIsHidden(!word.getIsHidden())
        wordDao.update(word)
    }
    
    suspend fun getWordsDueForReview(): List<Word> {
        return wordDao.getWordsDueForReview()
    }
    
    private fun calculateNextReviewDate(stage: Int): Date {
        val calendar = Calendar.getInstance()
        when (stage) {
            1 -> calendar.add(Calendar.DAY_OF_YEAR, 0)  // 立即复习
            2 -> calendar.add(Calendar.DAY_OF_YEAR, 1)  // 1天后
            3 -> calendar.add(Calendar.DAY_OF_YEAR, 3)  // 3天后
            4 -> calendar.add(Calendar.DAY_OF_YEAR, 6)  // 6天后
        }
        return calendar.time
    }
    
    suspend fun initializeWithDefaultWords() {
        // 此方法已弃用，使用DatabaseInitializer从CSV文件导入单词
        // 数据库初始化由DatabaseInitializer自动处理
        // 它会从kaoyan_words_100.csv导入100个考研重点词汇
    }
    
    private fun createWord(english: String, chinese: String): Word {
        val word = Word()
        word.english = english
        word.chinese = chinese
        return word
    }
    
    suspend fun getAllReviewWords(): List<Word> {
        return wordDao.getAllReviewWords()
    }
    
    suspend fun getNextReviewWord(): com.example.words.data.Word? {
        return wordDao.getNextReviewWord()
    }
    
    suspend fun getWordById(id: Long): Word? {
        return wordDao.getWordById(id)
    }
    
    suspend fun markReviewAsRemembered(word: com.example.words.data.Word) {
        // 完成1次复习就标记为认识
        word.status = "KNOWN"
        word.reviewStage = 0
        word.nextReviewDate = null
        word.lastReviewedDate = Date()
        wordDao.update(word)
    }
    
    suspend fun markReviewAsForgotten(word: com.example.words.data.Word) {
        // 忘记单词，重新从第一阶段开始复习（立即）
        word.reviewStage = 1
        word.lastReviewedDate = Date()
        word.nextReviewDate = calculateNextReviewDate(1)  // 立即复习
        wordDao.update(word)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: WordRepository? = null
        
        fun getRepository(context: Context): WordRepository {
            return INSTANCE ?: synchronized(this) {
                val database = WordDatabase.getDatabase(context)
                val instance = WordRepository(database.wordDao())
                INSTANCE = instance
                instance
            }
        }
    }
}