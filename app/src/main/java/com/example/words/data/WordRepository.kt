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
        word.nextReviewDate = calculateNextReviewDate(1)
        wordDao.update(word)
    }
    
    suspend fun toggleHidden(word: Word) {
        word.setIsHidden(!word.getIsHidden())
        wordDao.update(word)
    }
    
    suspend fun getWordsDueForReview(): List<Word> {
        return wordDao.getWordsDueForReview(Date())
    }
    
    private fun calculateNextReviewDate(stage: Int): Date {
        val calendar = Calendar.getInstance()
        when (stage) {
            1 -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            2 -> calendar.add(Calendar.DAY_OF_YEAR, 2)
            3 -> calendar.add(Calendar.DAY_OF_YEAR, 4)
            4 -> calendar.add(Calendar.DAY_OF_YEAR, 7)
        }
        return calendar.time
    }
    
    suspend fun initializeWithDefaultWords() {
        val count = wordDao.getWordCount()
        if (count == 0) {
            val defaultWords = listOf(
                createWord("abandon", "放弃，抛弃"),
                createWord("ability", "能力，才能"),
                createWord("abnormal", "反常的，异常的"),
                createWord("abolish", "废除，取消"),
                createWord("abroad", "在国外，到国外"),
                createWord("absence", "缺席，缺乏"),
                createWord("absolute", "绝对的，完全的"),
                createWord("absorb", "吸收，吸引"),
                createWord("abstract", "抽象的，摘要"),
                createWord("abundant", "丰富的，充裕的"),
                createWord("academic", "学术的，学院的"),
                createWord("accelerate", "加速，促进"),
                createWord("access", "接近，进入，使用权"),
                createWord("accommodate", "容纳，提供住宿"),
                createWord("accompany", "陪伴，伴随"),
                createWord("accomplish", "完成，实现"),
                createWord("account", "账户，说明，解释"),
                createWord("accumulate", "积累，积聚"),
                createWord("accurate", "准确的，精确的"),
                createWord("accuse", "指责，控告")
            )
            for (word in defaultWords) {
                wordDao.insert(word)
            }
        }
    }
    
    private fun createWord(english: String, chinese: String): Word {
        val word = Word()
        word.english = english
        word.chinese = chinese
        return word
    }
    
    suspend fun getNextReviewWord(): com.example.words.data.Word? {
        val currentDate = Date()
        return wordDao.getNextReviewWord(currentDate)
    }
    
    suspend fun markReviewAsRemembered(word: com.example.words.data.Word) {
        if (word.reviewStage < 4) {
            word.reviewStage++
            word.lastReviewedDate = Date()
            word.nextReviewDate = calculateNextReviewDate(word.reviewStage)
            wordDao.update(word)
        } else {
            // 完成4次复习，标记为认识
            word.status = "KNOWN"
            word.reviewStage = 0
            word.nextReviewDate = null
            word.lastReviewedDate = Date()
            wordDao.update(word)
        }
    }
    
    suspend fun markReviewAsForgotten(word: com.example.words.data.Word) {
        // 忘记单词，重新从第一阶段开始复习
        word.reviewStage = 1
        word.lastReviewedDate = Date()
        word.nextReviewDate = calculateNextReviewDate(1)
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