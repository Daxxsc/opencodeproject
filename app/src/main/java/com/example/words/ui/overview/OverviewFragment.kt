package com.example.words.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.words.R
import com.example.words.databinding.FragmentOverviewBinding
import com.example.words.viewmodel.WordViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordViewModel by viewModels()
    private lateinit var wordAdapter: GroupedWordAdapter
    private var showHiddenOnly = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupMenu()
    }
    
    override fun onStart() {
        super.onStart()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        wordAdapter = GroupedWordAdapter { word ->
            showWordDetails(word)
        }
        
        binding.rvWords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wordAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allWords.collect { words ->
                val filteredWords = if (showHiddenOnly) {
                    words.filter { word -> word.getIsHidden() }
                } else {
                    words
                }
                wordAdapter.updateWords(filteredWords)
                updateStats(words)
            }
        }
    }

    private fun loadWords() {
        // allWords是Flow，会自动更新，不需要手动加载
    }

    private fun updateStats(words: List<com.example.words.data.Word>) {
        val knownCount = words.count { it.status == "KNOWN" && !it.getIsHidden() }
        val unknownCount = words.count { it.status == "UNKNOWN" && it.reviewStage == 0 && !it.getIsHidden() }
        val reviewCount = words.count { it.reviewStage > 0 && !it.getIsHidden() }
        val hiddenCount = words.count { it.getIsHidden() }

        binding.tvKnownCount.text = "认识: $knownCount"
        binding.tvUnknownCount.text = "不认识: $unknownCount"
        binding.tvReviewCount.text = "复习中: $reviewCount"
        binding.tvHiddenCount.text = "已隐藏: $hiddenCount"
    }

    private fun setupMenu() {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: android.view.MenuInflater) {
        inflater.inflate(R.menu.overview_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_toggle_hidden -> {
                showHiddenOnly = !showHiddenOnly
                item.title = if (showHiddenOnly) "仅显示隐藏" else "显示全部"
                // 重置展开状态，然后重新观察数据
                wordAdapter.resetExpandedStates()
                observeViewModel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showWordDetails(word: com.example.words.data.Word) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(word.english)
            .setMessage("中文: ${word.chinese}\n状态: ${getStatusText(word)}\n复习阶段: ${word.reviewStage}")
            .setPositiveButton(if (word.isHidden) "取消隐藏" else "隐藏") { _, _ ->
                toggleWordHidden(word)
            }
            .setNegativeButton("关闭", null)
            .create()
        dialog.show()
    }

    private fun getStatusText(word: com.example.words.data.Word): String {
        return when {
            word.getIsHidden() -> "已隐藏"
            word.status == "KNOWN" -> "认识"
            word.reviewStage > 0 -> "复习中"
            else -> "不认识"
        }
    }

    private fun toggleWordHidden(word: com.example.words.data.Word) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toggleHidden(word)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}