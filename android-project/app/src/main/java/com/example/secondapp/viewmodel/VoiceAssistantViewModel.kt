package com.example.secondapp.viewmodel

import android.app.Application
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.secondapp.api.ApiClient
import com.example.secondapp.api.DeepSeekRequest
import com.example.secondapp.api.Message as ApiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData for UI state
    private val _voiceInputText = MutableLiveData<String>()
    val voiceInputText: LiveData<String> = _voiceInputText

    private val _responseText = MutableLiveData<String>()
    val responseText: LiveData<String> = _responseText

    private val _recordingStatus = MutableLiveData<String>()
    val recordingStatus: LiveData<String> = _recordingStatus

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val _bottomStatus = MutableLiveData<String>()
    val bottomStatus: LiveData<String> = _bottomStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _volumeLevel = MutableLiveData<Int>()
    val volumeLevel: LiveData<Int> = _volumeLevel

    init {
        _voiceInputText.value = "请点击下方按钮开始说话..."
        _responseText.value = "等待您的语音输入..."
        _recordingStatus.value = "等待中"
        _isRecording.value = false
        _bottomStatus.value = "就绪"
        _errorMessage.value = ""
        _volumeLevel.value = 0
    }

    fun startRecording() {
        _isRecording.value = true
        _recordingStatus.value = "录音中..."
        _voiceInputText.value = "正在聆听..."
        _bottomStatus.value = "录音中 - 请说话"
    }

    fun stopRecording() {
        _isRecording.value = false
        _recordingStatus.value = "已停止"
        _bottomStatus.value = "就绪"

        // If we have recognized text, send it to DeepSeek
        val currentInput = _voiceInputText.value
        if (currentInput != null && currentInput != "正在聆听..." && currentInput != "请点击下方按钮开始说话...") {
            sendToDeepSeek(currentInput)
        } else {
            _responseText.value = "没有检测到语音输入"
        }
    }

    fun onSpeechResults(results: List<String>) {
        if (results.isNotEmpty()) {
            val recognizedText = results[0]
            _voiceInputText.value = recognizedText
            _bottomStatus.value = "识别成功，正在生成回复"
        }
    }

    fun onSpeechError(errorCode: Int) {
        val errorMessage = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "音频错误"
            SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
            SpeechRecognizer.ERROR_NETWORK -> "网络错误"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
            SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的语音"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
            SpeechRecognizer.ERROR_SERVER -> "服务器错误"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
            else -> "未知错误: $errorCode"
        }
        _errorMessage.value = errorMessage
        stopRecording()
    }

    private fun sendToDeepSeek(userInput: String) {
        viewModelScope.launch {
            try {
                _responseText.value = "思考中..."
                _bottomStatus.value = "正在与DeepSeek通信..."

                val response = withContext(Dispatchers.IO) {
                    val request = DeepSeekRequest(
                        messages = listOf(
                            ApiMessage(role = "user", content = userInput)
                        ),
                        maxTokens = 500,
                        temperature = 0.7
                    )

                    // Make sure API key is set
                    if (ApiClient.apiKey.isEmpty()) {
                        throw IllegalArgumentException("DeepSeek API密钥未设置")
                    }

                    ApiClient.deepSeekService.chatCompletion(
                        authorization = "Bearer ${ApiClient.apiKey}",
                        request = request
                    )
                }

                if (response.isSuccessful) {
                    val deepSeekResponse = response.body()
                    if (deepSeekResponse != null && deepSeekResponse.choices.isNotEmpty()) {
                        val assistantResponse = deepSeekResponse.choices[0].message.content
                        _responseText.value = assistantResponse
                        _bottomStatus.value = "回复已生成"
                    } else {
                        _responseText.value = "没有收到有效的回复"
                        _bottomStatus.value = "API返回空响应"
                    }
                } else {
                    _responseText.value = "API请求失败: ${response.code()}"
                    _bottomStatus.value = "请求失败"
                }
            } catch (e: Exception) {
                _responseText.value = "发生错误: ${e.message}"
                _bottomStatus.value = "错误"
                Timber.e(e, "DeepSeek API调用失败")
            }
        }
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun updateVolumeLevel(rmsdB: Float) {
        // Convert RMS dB to a 0-100 scale
        // Typical rmsdB range: -60 (quiet) to 0 (loud)
        val minDB = -60f
        val maxDB = 0f
        val normalized = ((rmsdB - minDB) / (maxDB - minDB)).coerceIn(0f, 1f)
        val level = (normalized * 100).toInt()
        _volumeLevel.postValue(level)
    }

    override fun onCleared() {
        super.onCleared()
        // No speech recognizer to clean up - handled by Activity
    }
}