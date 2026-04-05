package com.example.words.ui.learning

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.words.R
import com.example.words.databinding.FragmentLearningBinding
import com.example.words.viewmodel.WordViewModel
import kotlinx.coroutines.launch

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordViewModel by viewModels()
    
    // 当前学习阶段：1=第一阶段（认识/不认识），2=第二阶段（下一个/记错了）
    private var currentStage = 1
    // 第一阶段的选择：true=认识，false=不认识
    private var firstStageChoice: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        loadNextWord()
    }

    private fun setupClickListeners() {
        // 第一阶段按钮
        binding.btnKnown.setOnClickListener {
            firstStageChoice = true
            enterSecondStage()
        }

        binding.btnUnknown.setOnClickListener {
            firstStageChoice = false
            enterSecondStage()
        }
        
        // 第二阶段按钮
        binding.btnNext.setOnClickListener {
            // 用户确认记忆正确
            if (firstStageChoice == true) {
                // 认识且记忆正确 -> 标记为认识
                markWordAsKnown()
            } else {
                // 第一阶段选择了"不认识"，无论记忆是否正确，都进入复习
                // 因为第一阶段就选择了"不认识"，说明单词不熟悉
                markWordAsUnknown()
            }
            resetToFirstStage()
            loadNextWord()
        }
        
        binding.btnWrong.setOnClickListener {
            // 用户记忆错误
            if (firstStageChoice == true) {
                // 认识但记忆错误 -> 实际是不认识，进入复习
                markWordAsUnknown()
            } else {
                // 第一阶段选择了"不认识"，无论记忆是否正确，都进入复习
                // 因为第一阶段就选择了"不认识"，说明单词不熟悉
                markWordAsUnknown()
            }
            resetToFirstStage()
            loadNextWord()
        }
    }

    private fun observeViewModel() {
        viewModel.currentWord.observe(viewLifecycleOwner) { word ->
            word?.let {
                binding.tvWord.text = it.english
                // 每次新单词都重置到第一阶段
                resetToFirstStage()
            }
        }
    }

    private fun enterSecondStage() {
        currentStage = 2
        
        // 显示中文翻译
        viewModel.currentWord.value?.let { word ->
            binding.tvTranslation.text = word.chinese
            binding.tvTranslation.setTextColor(resources.getColor(android.R.color.black, null))
        }
        
        // 切换按钮组
        binding.firstStageBtnGroup.visibility = View.GONE
        binding.secondStageBtnGroup.visibility = View.VISIBLE
        
        // 更新提示
        val choiceText = if (firstStageChoice == true) getString(R.string.btn_know) else getString(R.string.btn_unknown)
        binding.tvProgress.text = getString(R.string.hint_second_stage, choiceText)
    }
    
    private fun resetToFirstStage() {
        currentStage = 1
        firstStageChoice = null
        
        // 重置翻译显示
        binding.tvTranslation.text = getString(R.string.hint_click_to_show_translation)
        binding.tvTranslation.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        
        // 切换按钮组
        binding.firstStageBtnGroup.visibility = View.VISIBLE
        binding.secondStageBtnGroup.visibility = View.GONE
        
        // 重置提示
        updateProgress()
    }

    private fun markWordAsKnown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markAsKnown()
        }
    }

    private fun markWordAsUnknown() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentWord = viewModel.currentWord.value
            Log.d("LearningFragment", "标记单词为不认识: ${currentWord?.english}")
            viewModel.markAsUnknown()
            // 检查单词是否已标记为REVIEWING
            val updatedWord = viewModel.currentWord.value
            Log.d("LearningFragment", "标记后单词状态: ${updatedWord?.status}, reviewStage: ${updatedWord?.reviewStage}")
        }
    }

    private fun loadNextWord() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadRandomUnknownWord()
        }
    }

    private fun updateProgress() {
        // 显示当前学习状态
        when (currentStage) {
            1 -> binding.tvProgress.text = getString(R.string.hint_first_stage)
            2 -> {
                val choiceText = if (firstStageChoice == true) getString(R.string.btn_know) else getString(R.string.btn_unknown)
                binding.tvProgress.text = getString(R.string.hint_second_stage, choiceText)
            }
        }
        binding.progressBar.progress = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}