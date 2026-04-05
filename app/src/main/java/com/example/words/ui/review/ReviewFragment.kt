package com.example.words.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.example.words.databinding.FragmentReviewBinding
import com.example.words.viewmodel.WordViewModel
import kotlinx.coroutines.launch

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordViewModel by viewModels()

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
        loadNextReviewWord()
    }

    private fun setupClickListeners() {
        binding.tvWord.setOnClickListener {
            toggleTranslation()
        }

        binding.btnRemember.setOnClickListener {
            markWordAsRemembered()
        }

        binding.btnForget.setOnClickListener {
            markWordAsForgotten()
        }
    }

    private fun observeViewModel() {
        viewModel.currentReviewWord.observe(viewLifecycleOwner) { word ->
            word?.let {
                binding.tvWord.text = it.english
                binding.tvTranslation.text = "点击显示中文翻译"
                
                val reviewStage = it.reviewStage
                val days = when (reviewStage) {
                    1 -> "1天"
                    2 -> "2天"
                    3 -> "4天"
                    4 -> "7天"
                    else -> "完成"
                }
                binding.tvReviewInfo.text = "第${reviewStage}次复习 (${days}后)"
            }
        }
    }

    private fun toggleTranslation() {
        viewModel.currentReviewWord.value?.let { word ->
            if (binding.tvTranslation.text == "点击显示中文翻译") {
                binding.tvTranslation.text = word.chinese
            } else {
                binding.tvTranslation.text = "点击显示中文翻译"
            }
        }
    }

    private fun markWordAsRemembered() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markReviewWordAsRemembered()
            loadNextReviewWord()
        }
    }

    private fun markWordAsForgotten() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markReviewWordAsForgotten()
            loadNextReviewWord()
        }
    }

    private fun loadNextReviewWord() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadNextReviewWord()
        }
    }

    private fun updateProgress() {
        // 暂时不显示进度，可以后续从数据库获取
        binding.tvProgress.text = "复习模式"
        binding.progressBar.progress = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}