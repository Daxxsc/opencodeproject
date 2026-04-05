package com.example.words.ui.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.example.words.databinding.FragmentLearningBinding
import com.example.words.viewmodel.WordViewModel
import kotlinx.coroutines.launch

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordViewModel by viewModels()

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
        binding.tvWord.setOnClickListener {
            toggleTranslation()
        }

        binding.btnKnown.setOnClickListener {
            markWordAsKnown()
        }

        binding.btnUnknown.setOnClickListener {
            markWordAsUnknown()
        }
    }

    private fun observeViewModel() {
        viewModel.currentWord.observe(viewLifecycleOwner) { word ->
            word?.let {
                binding.tvWord.text = it.english
                binding.tvTranslation.text = "点击显示中文翻译"
                binding.tvTranslation.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleTranslation() {
        viewModel.currentWord.value?.let { word ->
            if (binding.tvTranslation.text == "点击显示中文翻译") {
                binding.tvTranslation.text = word.chinese
            } else {
                binding.tvTranslation.text = "点击显示中文翻译"
            }
        }
    }

    private fun markWordAsKnown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markAsKnown()
        }
    }

    private fun markWordAsUnknown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.markAsUnknown()
        }
    }

    private fun loadNextWord() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadRandomUnknownWord()
        }
    }

    private fun updateProgress() {
        // 暂时不显示进度，可以后续从数据库获取
        binding.tvProgress.text = "学习模式"
        binding.progressBar.progress = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}