package com.example.words.viewmodel

import android.app.Application
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
                repository.initializeWithDefaultWords()
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
                repository.markForReview(word)
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