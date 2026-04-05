package com.example.secondapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.secondapp.databinding.ActivitySimpleVoiceBinding
import java.util.Locale

/**
 * 简化的语音助手 Activity - 针对红米 K60 优化
 */
class SimpleVoiceActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySimpleVoiceBinding
    private lateinit var recordButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var resultTextView: TextView
    
    private var isRecording = false
    
    // 语音识别结果回调
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            
            if (results != null && results.isNotEmpty()) {
                val spokenText = results[0]
                resultTextView.text = "识别结果: $spokenText"
                statusTextView.text = "识别成功"
                
                // 这里可以添加处理语音指令的逻辑
                processVoiceCommand(spokenText)
            } else {
                resultTextView.text = "没有识别到语音"
                statusTextView.text = "识别失败"
            }
        } else {
            resultTextView.text = "用户取消或识别失败"
            statusTextView.text = "已取消"
        }
        
        isRecording = false
        updateButtonState()
    }
    
    // 权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show()
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "需要录音权限才能使用语音助手", Toast.LENGTH_LONG).show()
            statusTextView.text = "需要录音权限"
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeViews()
        setupClickListeners()
        
        // 检查设备是否支持语音识别
        checkSpeechRecognitionAvailability()
    }
    
    private fun initializeViews() {
        recordButton = binding.recordButton
        statusTextView = binding.statusTextView
        resultTextView = binding.resultTextView
    }
    
    private fun setupClickListeners() {
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }
    
    private fun startRecording() {
        if (checkPermission()) {
            startVoiceRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    private fun stopRecording() {
        // 对于系统 Intent 方式，无法直接停止
        // 这里只是更新 UI 状态
        isRecording = false
        updateButtonState()
        statusTextView.text = "已停止"
    }
    
    private fun startVoiceRecognition() {
        try {
            val intent = createSpeechIntent()
            
            // 检查是否有应用可以处理这个 Intent
            val activities = packageManager.queryIntentActivities(intent, 0)
            if (activities.isNotEmpty()) {
                isRecording = true
                updateButtonState()
                statusTextView.text = "正在聆听..."
                resultTextView.text = ""
                
                speechRecognitionLauncher.launch(intent)
            } else {
                Toast.makeText(this, "未找到语音识别应用", Toast.LENGTH_LONG).show()
                statusTextView.text = "设备不支持语音识别"
                
                // 提示用户安装 Google 语音搜索或其他语音识别应用
                showSpeechRecognitionHelp()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "启动语音识别失败: ${e.message}", Toast.LENGTH_LONG).show()
            statusTextView.text = "启动失败"
            isRecording = false
            updateButtonState()
        }
    }
    
    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 设置中文
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            
            // 针对 MIUI 的优化设置
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }
    }
    
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun updateButtonState() {
        if (isRecording) {
            recordButton.text = "停止录音"
            recordButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        } else {
            recordButton.text = "开始录音"
            recordButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        }
    }
    
    private fun checkSpeechRecognitionAvailability() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val activities = packageManager.queryIntentActivities(intent, 0)
        
        if (activities.isEmpty()) {
            statusTextView.text = "设备不支持语音识别"
            recordButton.isEnabled = false
            showSpeechRecognitionHelp()
        } else {
            val engineNames = activities.joinToString(", ") { it.loadLabel(packageManager).toString() }
            statusTextView.text = "就绪 (支持: $engineNames)"
        }
    }
    
    private fun processVoiceCommand(command: String) {
        val lowerCommand = command.lowercase(Locale.getDefault())
        
        when {
            lowerCommand.contains("你好") || lowerCommand.contains("嗨") -> {
                Toast.makeText(this, "你好！我是语音助手", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("时间") -> {
                val currentTime = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
                resultTextView.text = "当前时间: $currentTime\n$command"
            }
            lowerCommand.contains("日期") -> {
                val currentDate = java.text.SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(java.util.Date())
                resultTextView.text = "今天日期: $currentDate\n$command"
            }
            lowerCommand.contains("打开") -> {
                resultTextView.text = "收到打开指令: $command"
                Toast.makeText(this, "收到打开指令", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("关闭") || lowerCommand.contains("退出") -> {
                resultTextView.text = "收到关闭指令: $command"
                Toast.makeText(this, "收到关闭指令", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // 默认处理
                resultTextView.text = "你说: $command"
            }
        }
    }
    
    private fun showSpeechRecognitionHelp() {
        Toast.makeText(this, 
            "请确保已安装 Google 语音搜索或系统语音识别应用", 
            Toast.LENGTH_LONG
        ).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
    }
}