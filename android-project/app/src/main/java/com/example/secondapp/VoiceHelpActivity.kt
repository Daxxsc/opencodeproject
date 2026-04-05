package com.example.secondapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 语音识别帮助页面
 */
class VoiceHelpActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_help)
        
        val installButton: Button = findViewById(R.id.installButton)
        val retryButton: Button = findViewById(R.id.retryButton)
        val helpText: TextView = findViewById(R.id.helpText)
        
        helpText.text = """
            红米 K60 语音识别设置指南：
            
            1. 确保已安装语音识别应用：
               • 小爱同学（系统自带）
               • Google 语音搜索
               • 其他语音识别应用
            
            2. 检查权限设置：
               • 设置 → 应用管理 → 本应用 → 权限
               • 开启「麦克风」权限
            
            3. 系统语音设置：
               • 设置 → 更多设置 → 语言和输入法
               • 确保语音输入已启用
            
            4. 测试系统语音：
               • 长按 Home 键或语音键
               • 检查是否能调用语音助手
            """.trimIndent()
        
        installButton.setOnClickListener {
            // 打开 Google Play 商店搜索语音识别应用
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://search?q=语音识别")
                    setPackage("com.android.vending")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // 如果 Play 商店不可用，打开浏览器
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/search?q=语音识别&c=apps")
                }
                startActivity(intent)
            }
        }
        
        retryButton.setOnClickListener {
            // 返回主界面重试
            val intent = Intent(this, EasyVoiceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}