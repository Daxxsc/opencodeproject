package com.example.secondapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import java.util.Locale
import android.widget.TextView
import android.widget.Button
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.secondapp.databinding.ActivityMainBinding
import com.example.secondapp.api.ApiClient
import com.example.secondapp.viewmodel.VoiceAssistantViewModel
import com.google.android.material.button.MaterialButton
import timber.log.Timber
import android.widget.ProgressBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VoiceAssistantViewModel

    private lateinit var recordButton: MaterialButton
    private lateinit var voiceInputTextView: TextView
    private lateinit var recordingStatusTextView: TextView
    private lateinit var responseTextView: TextView
    private lateinit var bottomStatusTextView: TextView
    private lateinit var recordingIndicator: View
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var bluetoothButton: Button

    private var speechRecognizer: SpeechRecognizer? = null
    private var isUsingIntentMode = false
    private var hasShownSpeechRecognitionDialog = false

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.d("录音权限已授予")
            if (SpeechRecognizer.isRecognitionAvailable(this@MainActivity)) {
                if (initializeSpeechRecognizer()) {
                    isUsingIntentMode = false
                    Timber.d("使用SpeechRecognizer模式")
                } else {
                    // 初始化失败，尝试Intent方式
                    Timber.w("SpeechRecognizer初始化失败，检查Intent方式")
                    val intent = createSpeechIntent()
                    val activities = packageManager.queryIntentActivities(intent, 0)

                    if (activities.isNotEmpty()) {
                        isUsingIntentMode = true
                        Timber.d("Intent方式可用，找到${activities.size}个语音识别应用")
                        viewModel.setErrorMessage("将使用系统语音识别")
                    } else {
                        Timber.e("两种语音识别方式均不可用")
                        viewModel.setErrorMessage("未找到语音识别应用")
                        showSpeechRecognitionHelpDialog()
                    }
                }
            } else {
                Timber.w("设备不支持SpeechRecognizer，检查Intent方式")
                val intent = createSpeechIntent()
                val activities = packageManager.queryIntentActivities(intent, 0)

                if (activities.isNotEmpty()) {
                    isUsingIntentMode = true
                    Timber.d("Intent方式可用，找到${activities.size}个语音识别应用")
                    viewModel.setErrorMessage("将使用系统语音识别")
                } else {
                    Timber.e("两种语音识别方式均不可用")
                    viewModel.setErrorMessage("未找到语音识别应用")
                    showSpeechRecognitionHelpDialog()
                }
            }
        } else {
            Timber.w("录音权限被拒绝")
            viewModel.setErrorMessage("需要录音权限才能使用语音功能")
        }
    }

    // Speech recognition result launcher
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (matches != null && matches.isNotEmpty()) {
                    Timber.d("Intent识别结果: ${matches[0]}")
                    viewModel.onSpeechResults(matches)
                    viewModel.stopRecording()
                }
            }
        } else {
            Timber.d("语音识别取消或失败")
            viewModel.stopRecording()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Timber
        Timber.plant(Timber.DebugTree())

        // Debug speech recognition availability
        Timber.d("=== 语音识别兼容性检查 ===")
        Timber.d("SpeechRecognizer可用性: ${SpeechRecognizer.isRecognitionAvailable(this)}")

        val speechIntent = createSpeechIntent()
        val speechActivities = packageManager.queryIntentActivities(speechIntent, 0)
        Timber.d("可处理ACTION_RECOGNIZE_SPEECH的应用数量: ${speechActivities.size}")
        speechActivities.forEachIndexed { index, resolveInfo ->
            Timber.d("应用 ${index + 1}: ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}")
        }

        // Check for ACTION_WEB_SEARCH as alternative
        val webSearchIntent = Intent(RecognizerIntent.ACTION_WEB_SEARCH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
        }
        val webSearchActivities = packageManager.queryIntentActivities(webSearchIntent, 0)
        Timber.d("可处理ACTION_WEB_SEARCH的应用数量: ${webSearchActivities.size}")
        webSearchActivities.forEachIndexed { index, resolveInfo ->
            Timber.d("WEB搜索应用 ${index + 1}: ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}")
        }

        // Initialize views
        initViews()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[VoiceAssistantViewModel::class.java]

        // Setup observers
        setupObservers()

        // Setup click listeners
        setupClickListeners()

        // Check and request permission
        checkAndRequestPermission()

        // TODO: Set your DeepSeek API key here
        ApiClient.apiKey = "sk-1bf7b3ec213b45a185954fdc4e654432"
    }

    private fun initViews() {
        recordButton = binding.recordButton
        voiceInputTextView = binding.voiceInputTextView
        recordingStatusTextView = binding.recordingStatusTextView
        responseTextView = binding.responseTextView
        bottomStatusTextView = binding.bottomStatusTextView
        recordingIndicator = binding.recordingIndicator
        volumeProgressBar = binding.volumeProgressBar
        bluetoothButton = binding.bluetoothButton
    }

    private fun setupObservers() {
        viewModel.voiceInputText.observe(this) { text ->
            voiceInputTextView.text = text
        }

        viewModel.responseText.observe(this) { text ->
            responseTextView.text = text
        }

        viewModel.recordingStatus.observe(this) { status ->
            recordingStatusTextView.text = status
        }

        viewModel.isRecording.observe(this) { isRecording ->
            if (isRecording) {
                recordButton.text = "停止录音"
                recordingIndicator.setBackgroundResource(R.drawable.circle_red)
            } else {
                recordButton.text = "开始录音"
                recordingIndicator.setBackgroundResource(R.drawable.circle_gray)
            }
        }

        viewModel.bottomStatus.observe(this) { status ->
            bottomStatusTextView.text = status
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Timber.e("Error: $error")
                // Show error toast or dialog
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.volumeLevel.observe(this) { level ->
            volumeProgressBar.progress = level
            Timber.d("更新音量进度: $level")
        }
    }

    private fun setupClickListeners() {
        recordButton.setOnClickListener {
            if (viewModel.isRecording.value == true) {
                viewModel.stopRecording()
                if (!isUsingIntentMode) {
                    speechRecognizer?.stopListening()
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ensureSpeechRecognizer()) {
                        viewModel.startRecording()
                        if (isUsingIntentMode) {
                            Timber.d("使用Intent方式启动语音识别")
                            speechRecognitionLauncher.launch(createSpeechIntent())
                        } else {
                            Timber.d("使用SpeechRecognizer启动语音识别")
                            speechRecognizer?.startListening(createSpeechIntent())
                        }
                    }
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }

        bluetoothButton.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("录音权限已拥有")
                if (SpeechRecognizer.isRecognitionAvailable(this)) {
                    if (initializeSpeechRecognizer()) {
                        isUsingIntentMode = false
                        Timber.d("使用SpeechRecognizer模式")
                    } else {
                        // 初始化失败，尝试Intent方式
                        Timber.w("SpeechRecognizer初始化失败，检查Intent方式")
                        val intent = createSpeechIntent()
                        val activities = packageManager.queryIntentActivities(intent, 0)

                        if (activities.isNotEmpty()) {
                            isUsingIntentMode = true
                            Timber.d("Intent方式可用，找到${activities.size}个语音识别应用")
                        } else {
                            Timber.e("两种语音识别方式均不可用")
                            viewModel.setErrorMessage("未找到语音识别应用")
                            showSpeechRecognitionHelpDialog()
                        }
                    }
                } else {
                    Timber.w("设备不支持SpeechRecognizer，检查Intent方式")
                    val intent = createSpeechIntent()
                    val activities = packageManager.queryIntentActivities(intent, 0)

                    if (activities.isNotEmpty()) {
                        isUsingIntentMode = true
                        Timber.d("Intent方式可用，找到${activities.size}个语音识别应用")
                    } else {
                        Timber.e("两种语音识别方式均不可用")
                        viewModel.setErrorMessage("未找到语音识别应用")
                        showSpeechRecognitionHelpDialog()
                    }
                }
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show explanation dialog
                android.app.AlertDialog.Builder(this)
                    .setTitle("需要录音权限")
                    .setMessage("语音助手需要录音权限来识别您的语音")
                    .setPositiveButton("确定") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun initializeSpeechRecognizer(): Boolean {
        return try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : android.speech.RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Timber.d("准备就绪，可以开始说话")
                    }

                    override fun onBeginningOfSpeech() {
                        Timber.d("开始说话")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        viewModel.updateVolumeLevel(rmsdB)
                        Timber.d("音量级别: $rmsdB dB")
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Not used
                    }

                    override fun onEndOfSpeech() {
                        Timber.d("结束说话")
                    }

                    override fun onError(error: Int) {
                        Timber.e("语音识别错误: $error")
                        runOnUiThread {
                            viewModel.onSpeechError(error)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (matches != null && matches.isNotEmpty()) {
                            Timber.d("识别结果: ${matches[0]}")
                            viewModel.onSpeechResults(matches)
                            // Stop recording and send to API
                            runOnUiThread {
                                viewModel.stopRecording()
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        // Optional: Handle partial results
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Not used
                    }
                })
            }
            Timber.d("语音识别器初始化成功")
            true
        } catch (e: Exception) {
            Timber.e(e, "语音识别器初始化失败")
            runOnUiThread {
                viewModel.setErrorMessage("语音识别初始化失败: ${e.message}")
            }
            false
        }
    }

    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    private fun ensureSpeechRecognizer(): Boolean {
        if (speechRecognizer == null) {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                Timber.w("SpeechRecognizer不可用，检查Intent方式")
                // 检查是否有应用可以处理语音识别Intent
                val intent = createSpeechIntent()
                val pm = packageManager
                val activities = pm.queryIntentActivities(intent, 0)

                Timber.d("找到可处理语音识别Intent的应用: ${activities.size}个")
                activities.forEachIndexed { index, resolveInfo ->
                    Timber.d("  ${index + 1}. ${resolveInfo.activityInfo.packageName}")
                }

                val isIntentAvailable = activities.isNotEmpty()

                if (isIntentAvailable) {
                    Timber.d("Intent方式可用，使用系统语音识别")
                    isUsingIntentMode = true
                    return true
                } else {
                    Timber.e("语音识别不可用：SpeechRecognizer和Intent方式均不可用")
                    runOnUiThread {
                        viewModel.setErrorMessage("语音识别功能不可用")
                        showSpeechRecognitionHelpDialog()
                    }
                    return false
                }
            }
            if (initializeSpeechRecognizer()) {
                isUsingIntentMode = false
                Timber.d("SpeechRecognizer初始化成功，使用API模式")
            } else {
                // 初始化失败，尝试使用Intent方式
                Timber.w("SpeechRecognizer初始化失败，检查Intent方式")
                val intent = createSpeechIntent()
                val pm = packageManager
                val activities = pm.queryIntentActivities(intent, 0)

                Timber.d("找到可处理语音识别Intent的应用: ${activities.size}个")
                activities.forEachIndexed { index, resolveInfo ->
                    Timber.d("  ${index + 1}. ${resolveInfo.activityInfo.packageName}")
                }

                val isIntentAvailable = activities.isNotEmpty()

                if (isIntentAvailable) {
                    Timber.d("Intent方式可用，使用系统语音识别")
                    isUsingIntentMode = true
                    return true
                } else {
                    Timber.e("语音识别不可用：SpeechRecognizer初始化失败且Intent方式不可用")
                    runOnUiThread {
                        viewModel.setErrorMessage("语音识别功能不可用")
                        showSpeechRecognitionHelpDialog()
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun showSpeechRecognitionHelpDialog() {
        if (hasShownSpeechRecognitionDialog) return

        hasShownSpeechRecognitionDialog = true

        android.app.AlertDialog.Builder(this)
            .setTitle("语音识别不可用")
            .setMessage("您的设备不支持语音识别功能。\n\n可能的原因：\n1. 未安装语音识别应用\n2. 系统语音服务被禁用\n\n解决方案：\n• 安装Google语音搜索（需Google服务）\n• 安装百度语音、讯飞输入法等第三方应用\n• 在系统设置中启用语音输入功能")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("安装应用") { dialog, _ ->
                dialog.dismiss()
                // 尝试打开应用商店搜索语音识别应用
                try {
                    // 尝试Google Play Store
                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("market://search?q=语音识别")
                    }
                    startActivity(playStoreIntent)
                } catch (e: Exception) {
                    Timber.e(e, "无法打开应用商店，尝试浏览器")
                    try {
                        // 尝试浏览器搜索
                        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://play.google.com/store/search?q=语音识别&c=apps")
                        }
                        startActivity(browserIntent)
                    } catch (e2: Exception) {
                        Timber.e(e2, "无法打开浏览器")
                        // 最后尝试小米应用商店
                        try {
                            val miStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("mimarket://search?q=语音识别")
                            }
                            startActivity(miStoreIntent)
                        } catch (e3: Exception) {
                            Timber.e(e3, "无法打开小米应用商店")
                            viewModel.setErrorMessage("无法打开应用商店，请手动安装语音识别应用")
                        }
                    }
                }
            }
            .setNeutralButton("不再显示") { dialog, _ ->
                dialog.dismiss()
                hasShownSpeechRecognitionDialog = true
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}