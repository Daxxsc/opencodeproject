package com.example.secondapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * 语音识别助手 - 针对红米 K60 等 MIUI 设备优化
 */
class VoiceRecognitionHelper(
    private val context: Context,
    private val callback: VoiceRecognitionCallback
) {
    
    interface VoiceRecognitionCallback {
        fun onReadyForSpeech()
        fun onBeginningOfSpeech()
        fun onRmsChanged(rmsdB: Float)
        fun onBufferReceived(buffer: ByteArray)
        fun onEndOfSpeech()
        fun onError(errorCode: Int)
        fun onResults(results: List<String>)
        fun onPartialResults(partialResults: List<String>)
        fun onEvent(eventType: Int, params: Bundle?)
    }
    
    companion object {
        private const val TAG = "VoiceRecognitionHelper"
        
        // 检查设备是否支持语音识别
        fun isSpeechRecognitionAvailable(context: Context): Boolean {
            return try {
                // 检查系统是否有语音识别服务
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                val activities = context.packageManager.queryIntentActivities(intent, 0)
                activities.isNotEmpty() || SpeechRecognizer.isRecognitionAvailable(context)
            } catch (e: Exception) {
                Log.e(TAG, "检查语音识别可用性失败", e)
                false
            }
        }
        
        // 获取可用的语音识别引擎
        fun getAvailableEngines(context: Context): List<String> {
            return try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                val activities = context.packageManager.queryIntentActivities(intent, 0)
                activities.map { it.activityInfo.packageName }
            } catch (e: Exception) {
                Log.e(TAG, "获取语音识别引擎失败", e)
                emptyList()
            }
        }
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var useSystemIntent = false
    
    // 初始化语音识别
    fun initialize(): Boolean {
        return try {
            // 先尝试使用 SpeechRecognizer API
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                initializeSpeechRecognizer()
                true
            } else {
                // 回退到系统 Intent 方式
                useSystemIntent = true
                Log.d(TAG, "使用系统 Intent 方式进行语音识别")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "语音识别初始化失败", e)
            false
        }
    }
    
    private fun initializeSpeechRecognizer(): Boolean {
        return try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "语音识别准备就绪")
                        callback.onReadyForSpeech()
                    }
                    
                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "开始说话")
                        callback.onBeginningOfSpeech()
                    }
                    
                    override fun onRmsChanged(rmsdB: Float) {
                        callback.onRmsChanged(rmsdB)
                    }
                    
                    override fun onBufferReceived(buffer: ByteArray) {
                        callback.onBufferReceived(buffer)
                    }
                    
                    override fun onEndOfSpeech() {
                        Log.d(TAG, "说话结束")
                        callback.onEndOfSpeech()
                    }
                    
                    override fun onError(errorCode: Int) {
                        Log.e(TAG, "语音识别错误: $errorCode")
                        isListening = false
                        
                        // 处理特定错误
                        when (errorCode) {
                            SpeechRecognizer.ERROR_NO_MATCH -> {
                                Log.w(TAG, "没有匹配的语音输入")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                Log.w(TAG, "语音输入超时")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_AUDIO -> {
                                Log.e(TAG, "音频错误")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_CLIENT -> {
                                Log.e(TAG, "客户端错误")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                Log.e(TAG, "权限不足")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_NETWORK -> {
                                Log.e(TAG, "网络错误")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                Log.e(TAG, "网络超时")
                                callback.onError(errorCode)
                            }
                            SpeechRecognizer.ERROR_SERVER -> {
                                Log.e(TAG, "服务器错误")
                                callback.onError(errorCode)
                            }
                            else -> {
                                Log.e(TAG, "未知错误: $errorCode")
                                callback.onError(errorCode)
                            }
                        }
                    }
                    
                    override fun onResults(results: Bundle?) {
                        Log.d(TAG, "语音识别结果")
                        isListening = false
                        
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (matches != null && matches.isNotEmpty()) {
                            Log.d(TAG, "识别结果: ${matches[0]}")
                            callback.onResults(matches)
                        } else {
                            Log.w(TAG, "没有识别结果")
                            callback.onResults(emptyList())
                        }
                    }
                    
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (matches != null && matches.isNotEmpty()) {
                            callback.onPartialResults(matches)
                        }
                    }
                    
                    override fun onEvent(eventType: Int, params: Bundle?) {
                        callback.onEvent(eventType, params)
                    }
                })
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer 初始化失败", e)
            false
        }
    }
    
    // 开始语音识别
    fun startListening(): Boolean {
        return try {
            if (useSystemIntent) {
                // 使用系统 Intent
                val intent = createSpeechIntent()
                context.startActivity(intent)
                true
            } else {
                // 使用 SpeechRecognizer
                speechRecognizer?.let { recognizer ->
                    val intent = createSpeechIntent()
                    recognizer.startListening(intent)
                    isListening = true
                    true
                } ?: run {
                    Log.e(TAG, "SpeechRecognizer 未初始化")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "开始语音识别失败", e)
            false
        }
    }
    
    // 停止语音识别
    fun stopListening() {
        try {
            if (isListening) {
                speechRecognizer?.stopListening()
                isListening = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止语音识别失败", e)
        }
    }
    
    // 取消语音识别
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "取消语音识别失败", e)
        }
    }
    
    // 销毁资源
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "销毁语音识别资源失败", e)
        }
    }
    
    // 创建语音识别 Intent
    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 设置语言为中文
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            
            // 针对 MIUI 设备的特殊设置
            if (Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)) {
                putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR")
                putExtra("android.speech.extra.GET_AUDIO", true)
            }
        }
    }
    
    // 检查权限
    fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}