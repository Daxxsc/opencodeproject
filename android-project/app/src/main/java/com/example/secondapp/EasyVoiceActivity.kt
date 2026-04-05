package com.example.secondapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

/**
 * 最简单的语音助手 - 100% 使用系统语音识别
 */
class EasyVoiceActivity : AppCompatActivity() {
    
    private lateinit var speakButton: Button
    private lateinit var resultText: TextView
    private lateinit var statusText: TextView
    
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val REQUEST_VOICE_RECOGNITION = 300
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easy_voice)
        
        speakButton = findViewById(R.id.speakButton)
        resultText = findViewById(R.id.resultText)
        statusText = findViewById(R.id.statusText)
        
        speakButton.setOnClickListener {
            startVoiceRecognition()
        }
        
        // 检查权限
        checkPermission()
    }
    
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            statusText.text = "就绪 - 点击按钮开始说话"
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    statusText.text = "就绪 - 点击按钮开始说话"
                    Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show()
                } else {
                    statusText.text = "需要录音权限"
                    Toast.makeText(this, "需要录音权限才能使用语音功能", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun startVoiceRecognition() {
        // 检查设备是否支持语音识别
        if (!isSpeechRecognitionAvailable()) {
            // 跳转到帮助页面
            val intent = Intent(this, VoiceHelpActivity::class.java)
            startActivity(intent)
            return
        }
        
        // 创建语音识别 Intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 使用系统默认语言
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            
            // 尝试设置中文，如果系统支持
            try {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.language)
            } catch (e: Exception) {
                // 使用默认语言
            }
        }
        
        try {
            startActivityForResult(intent, REQUEST_VOICE_RECOGNITION)
            statusText.text = "正在聆听..."
        } catch (e: Exception) {
            Toast.makeText(this, "无法启动语音识别: ${e.message}", Toast.LENGTH_LONG).show()
            statusText.text = "启动失败"
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_VOICE_RECOGNITION -> {
                if (resultCode == RESULT_OK && data != null) {
                    val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    
                    if (results != null && results.isNotEmpty()) {
                        val spokenText = results[0]
                        resultText.text = "你说: $spokenText"
                        statusText.text = "识别成功"
                        
                        // 处理语音指令
                        processVoiceCommand(spokenText)
                    } else {
                        resultText.text = "没有识别到语音"
                        statusText.text = "识别失败"
                    }
                } else {
                    resultText.text = "识别取消或失败"
                    statusText.text = "已取消"
                }
            }
        }
    }
    
    private fun processVoiceCommand(command: String) {
        val lowerCommand = command.lowercase(Locale.getDefault())
        
        when {
            lowerCommand.contains("你好") || lowerCommand.contains("嗨") -> {
                Toast.makeText(this, "你好！", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("时间") -> {
                val currentTime = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
                resultText.text = "当前时间: $currentTime\n你说: $command"
            }
            lowerCommand.contains("日期") -> {
                val currentDate = java.text.SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(java.util.Date())
                resultText.text = "今天日期: $currentDate\n你说: $command"
            }
            lowerCommand.contains("打开") -> {
                Toast.makeText(this, "收到打开指令", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("关闭") -> {
                Toast.makeText(this, "收到关闭指令", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("测试") -> {
                Toast.makeText(this, "语音识别功能正常", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 默认显示识别结果
                Toast.makeText(this, "已识别: $command", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun isSpeechRecognitionAvailable(): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.isNotEmpty()
    }
}