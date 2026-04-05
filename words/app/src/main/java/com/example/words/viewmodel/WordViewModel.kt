package com.example.words.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.words.data.Word
import com.example.words.data.WordRepository
import kotlinx.coroutines.launch

class WordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = WordRepository.getRepository(application)
    
    private val _currentWord = MutableLiveData<Word?>()
    val currentWord: LiveData<Word?> = _currentWord
    
    private val _showTranslation = MutableLiveData(false)
    val showTranslation: LiveData<Boolean> = _showTranslation
    
    val allWords = repository.getAllWords()
    
    private val _currentReviewWord = MutableLiveData<com.example.words.data.Word?>()
    val currentReviewWord: androidx.lifecycle.LiveData<com.example.words.data.Word?> = _currentReviewWord
    
    init {
        viewModelScope.launch {
            try {
                // 数据库初始化由DatabaseInitializer自动处理
                // 它会从kaoyan_words_100.csv导入100个单词
                loadRandomUnknownWord()
                loadNextReviewWord()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun loadRandomUnknownWord() {
        viewModelScope.launch {
            val word = repository.getRandomUnknownWord()
            _currentWord.value = word
            _showTranslation.value = false
        }
    }
    
    fun showTranslation() {
        _showTranslation.value = true
    }
    
    fun markAsKnown() {
        viewModelScope.launch {
            _currentWord.value?.let { word ->
                repository.markAsKnown(word)
                loadRandomUnknownWord()
            }
        }
    }
    
    fun markAsUnknown() {
        viewModelScope.launch {
            _currentWord.value?.let { word ->
                Log.d("WordViewModel", "调用markForReview: ${word.english}, 当前状态: ${word.status}")
                repository.markForReview(word)
                // 重新加载当前单词以检查状态
                val updatedWord = repository.getWordById(word.id)
                Log.d("WordViewModel", "标记后状态: ${updatedWord?.status}, reviewStage: ${updatedWord?.reviewStage}")
                loadRandomUnknownWord()
            }
        }
    }
    
    fun toggleHidden(word: Word) {
        viewModelScope.launch {
            repository.toggleHidden(word)
        }
    }
    
    fun loadNextReviewWord() {
        viewModelScope.launch {
            val word = repository.getNextReviewWord()
            _currentReviewWord.value = word
        }
    }
    
    fun markReviewWordAsRemembered() {
        viewModelScope.launch {
            _currentReviewWord.value?.let { word ->
                repository.markReviewAsRemembered(word)
                loadNextReviewWord()
            }
        }
    }
    
    fun markReviewWordAsForgotten() {
        viewModelScope.launch {
            _currentReviewWord.value?.let { word ->
                repository.markReviewAsForgotten(word)
                loadNextReviewWord()
            }
        }
    }
}