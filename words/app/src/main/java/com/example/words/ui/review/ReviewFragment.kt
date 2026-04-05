package com.example.words.ui.review

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.words.data.WordDatabase
import com.example.words.databinding.FragmentReviewBinding
import com.example.words.viewmodel.WordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordViewModel by viewModels()
    
    // 复习阶段管理
    private var currentStage = 1 // 1: 第一阶段, 2: 第二阶段
    private var firstStageChoice: Boolean? = null // true: 记住了, false: 忘记了

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        // 初始化到第一阶段
        resetToFirstStage()
        // 强制刷新复习单词
        refreshReviewWords()
        
        // 调试：显示复习单词数量
        updateReviewCount()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次进入复习界面都刷新数据
        refreshReviewWords()
        updateReviewCount()
    }
    
    private fun refreshReviewWords() {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("ReviewFragment", "刷新复习单词列表")
            loadNextReviewWord()
        }
    }

    private fun setupClickListeners() {
        binding.tvWord.setOnClickListener {
            toggleTranslation()
        }

        // 第一阶段按钮
        binding.btnRemember.setOnClickListener {
            firstStageChoice = true // 记住了
            enterSecondStage()
        }

        binding.btnForget.setOnClickListener {
            firstStageChoice = false // 忘记了
            enterSecondStage()
        }
        
        // 第二阶段按钮
        binding.btnCorrect.setOnClickListener {
            // 用户确认记忆正确
            if (firstStageChoice == true) {
                // 记住了且记忆正确 -> 完成复习
                confirmWordAsRemembered()
            } else {
                // 忘记了且记忆正确 -> 继续复习（第一阶段选择了忘记就要复习）
                confirmWordAsForgotten()
            }
            resetToFirstStage()
        }
        
        binding.btnWrong.setOnClickListener {
            // 用户记忆错误
            if (firstStageChoice == true) {
                // 记住了但记忆错误 -> 继续复习
                confirmWordAsForgotten()
            } else {
                // 忘记了但记忆错误 -> 继续复习（第一阶段选择了忘记就要复习）
                confirmWordAsForgotten()
            }
            resetToFirstStage()
        }
    }

    private fun observeViewModel() {
        viewModel.currentReviewWord.observe(viewLifecycleOwner) { word ->
            if (word == null) {
                showNoReviewWords()
                return@observe
            }
            
            // 显示新单词时重置到第一阶段
            resetToFirstStage()
            
            // 显示英文单词
            binding.tvWord.text = word.english
            binding.tvTranslation.text = "点击显示中文翻译"
            
            // 启用按钮
            binding.btnRemember.isEnabled = true
            binding.btnForget.isEnabled = true
            binding.btnCorrect.isEnabled = true
            binding.btnWrong.isEnabled = true
        }
    }
    
    private fun showNoReviewWords() {
        binding.tvWord.text = "暂无需要复习的单词"
        binding.tvTranslation.text = "请在学习界面标记不认识的单词"
        binding.tvReviewInfo.text = "复习模式"
        // 禁用所有按钮
        binding.btnRemember.isEnabled = false
        binding.btnForget.isEnabled = false
        binding.btnCorrect.isEnabled = false
        binding.btnWrong.isEnabled = false
        // 确保显示第一阶段按钮组
        binding.btnGroup.visibility = View.VISIBLE
        binding.secondStageBtnGroup.visibility = View.GONE
    }

    private fun toggleTranslation() {
        // 只在第一阶段可以点击显示/隐藏翻译
        if (currentStage != 1) {
            return
        }
        
        val currentWord = viewModel.currentReviewWord.value
        if (currentWord == null) {
            binding.tvTranslation.text = "暂无单词可显示"
            return
        }
        
        if (binding.tvTranslation.text == "点击显示中文翻译") {
            binding.tvTranslation.text = currentWord.chinese
            binding.tvTranslation.setTextColor(resources.getColor(android.R.color.black, null))
        } else {
            binding.tvTranslation.text = "点击显示中文翻译"
            binding.tvTranslation.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }

    private fun confirmWordAsRemembered() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markReviewWordAsRemembered()
            loadNextReviewWord()
            updateReviewCount()
        }
    }

    private fun confirmWordAsForgotten() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markReviewWordAsForgotten()
            loadNextReviewWord()
            updateReviewCount()
        }
    }
    
    private fun enterSecondStage() {
        currentStage = 2
        
        // 显示中文翻译
        viewModel.currentReviewWord.value?.let { word ->
            binding.tvTranslation.text = word.chinese
            binding.tvTranslation.setTextColor(resources.getColor(android.R.color.black, null))
        }
        
        // 切换按钮组
        binding.btnGroup.visibility = View.GONE
        binding.secondStageBtnGroup.visibility = View.VISIBLE
        
        // 更新提示
        val choiceText = if (firstStageChoice == true) "记住了" else "忘记了"
        binding.tvReviewInfo.text = "确认: 你选择了'$choiceText'，记忆是否正确？"
    }
    
    private fun resetToFirstStage() {
        currentStage = 1
        firstStageChoice = null
        
        // 重置翻译显示
        binding.tvTranslation.text = "点击显示中文翻译"
        binding.tvTranslation.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        
        // 切换按钮组
        binding.btnGroup.visibility = View.VISIBLE
        binding.secondStageBtnGroup.visibility = View.GONE
        
        // 重置提示
        updateReviewInfo()
    }
    
    private fun updateReviewInfo() {
        viewModel.currentReviewWord.value?.let { word ->
            binding.tvReviewInfo.text = "复习单词 (1次复习后标记为认识)"
        }
    }
    
    private fun updateReviewCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                val database = WordDatabase.getDatabase(requireContext())
                database.wordDao().getDueReviewCount()
            }
            binding.tvProgress.text = "复习单词数量: $count"
        }
    }

    private fun loadNextReviewWord() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadNextReviewWord()
        }
    }

    private fun updateProgress() {
        // 显示复习单词数量
        viewLifecycleOwner.lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                val database = WordDatabase.getDatabase(requireContext())
                database.wordDao().getDueReviewCount()
            }
            binding.tvProgress.text = "复习单词数量: $count"
            binding.progressBar.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}