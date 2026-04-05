package com.example.words.ui.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.words.R
import com.example.words.data.Word

sealed class OverviewItem {
    data class Header(val title: String, val count: Int, val isExpanded: Boolean = true) : OverviewItem()
    data class WordItem(val word: Word) : OverviewItem()
}

class GroupedWordAdapter(
    private val onWordClick: (Word) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_WORD = 1
    }

    private var items: List<OverviewItem> = emptyList()
    private val expandedStates = mutableMapOf<String, Boolean>()

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OverviewItem.Header -> VIEW_TYPE_HEADER
            is OverviewItem.WordItem -> VIEW_TYPE_WORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_WORD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_word, parent, false)
                WordViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is OverviewItem.Header -> (holder as HeaderViewHolder).bind(item)
            is OverviewItem.WordItem -> (holder as WordViewHolder).bind(item.word)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvGroupTitle)
        private val tvCount: TextView = itemView.findViewById(R.id.tvGroupCount)
        private val ivExpandIndicator: TextView = itemView.findViewById(R.id.ivExpandIndicator)

        fun bind(header: OverviewItem.Header) {
            tvTitle.text = header.title
            tvCount.text = "(${header.count})"
            
            // 设置展开状态指示器
            val indicator = if (header.isExpanded) "▼" else "▶"
            ivExpandIndicator.text = indicator
            
            // 设置点击事件
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleGroupExpansion(position)
                }
            }
        }
        
        private fun toggleGroupExpansion(headerPosition: Int) {
            val header = items[headerPosition] as OverviewItem.Header
            val groupTitle = header.title
            
            // 切换展开状态
            val newExpandedState = !header.isExpanded
            expandedStates[groupTitle] = newExpandedState
            
            // 重新构建items列表
            rebuildItems()
        }
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEnglish: TextView = itemView.findViewById(R.id.tvEnglish)
        private val tvChinese: TextView = itemView.findViewById(R.id.tvChinese)
        private val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)

        fun bind(word: Word) {
            tvEnglish.text = word.english
            tvChinese.text = word.chinese
            
            val (statusText, bgResId) = when {
                word.getIsHidden() -> Pair("已隐藏", R.drawable.badge_hidden)
                word.status == "KNOWN" -> Pair("认识", R.drawable.badge_known)
                word.reviewStage > 0 -> Pair("复习中", R.drawable.badge_reviewing)
                else -> Pair("不认识", R.drawable.badge_unknown)
            }
            
            statusBadge.text = statusText
            statusBadge.setBackgroundResource(bgResId)
            
            itemView.setOnClickListener {
                onWordClick(word)
            }
        }
    }

    fun updateWords(words: List<Word>) {
        // 保存当前所有单词
        this.allWords = words
        
        // 构建分组
        rebuildItems()
    }
    
    fun resetExpandedStates() {
        expandedStates.clear()
        rebuildItems()
    }
    
    private fun rebuildItems() {
        val groupedItems = mutableListOf<OverviewItem>()
        
        // 检查是否只显示隐藏的单词
        val hasHiddenWords = allWords.any { it.getIsHidden() }
        val hasNonHiddenWords = allWords.any { !it.getIsHidden() }
        
        if (hasHiddenWords && !hasNonHiddenWords) {
            // 只显示隐藏的单词
            val hiddenWords = allWords.filter { it.getIsHidden() }
            if (hiddenWords.isNotEmpty()) {
                val isExpanded = expandedStates.getOrDefault("已隐藏的单词", true)
                groupedItems.add(OverviewItem.Header("已隐藏的单词", hiddenWords.size, isExpanded))
                if (isExpanded) {
                    hiddenWords.forEach { word ->
                        groupedItems.add(OverviewItem.WordItem(word))
                    }
                }
            }
        } else {
            // 显示所有分组（包括隐藏单词分组）
            // 分组：认识的单词
            val knownWords = allWords.filter { it.status == "KNOWN" && !it.getIsHidden() }
            if (knownWords.isNotEmpty()) {
                val isExpanded = expandedStates.getOrDefault("认识的单词", true)
                groupedItems.add(OverviewItem.Header("认识的单词", knownWords.size, isExpanded))
                if (isExpanded) {
                    knownWords.forEach { word ->
                        groupedItems.add(OverviewItem.WordItem(word))
                    }
                }
            }
            
            // 分组：不认识的单词
            val unknownWords = allWords.filter { it.status == "UNKNOWN" && it.reviewStage == 0 && !it.getIsHidden() }
            if (unknownWords.isNotEmpty()) {
                val isExpanded = expandedStates.getOrDefault("不认识的单词", true)
                groupedItems.add(OverviewItem.Header("不认识的单词", unknownWords.size, isExpanded))
                if (isExpanded) {
                    unknownWords.forEach { word ->
                        groupedItems.add(OverviewItem.WordItem(word))
                    }
                }
            }
            
            // 分组：复习中的单词
            val reviewingWords = allWords.filter { it.reviewStage > 0 && !it.getIsHidden() }
            if (reviewingWords.isNotEmpty()) {
                val isExpanded = expandedStates.getOrDefault("复习中的单词", true)
                groupedItems.add(OverviewItem.Header("复习中的单词", reviewingWords.size, isExpanded))
                if (isExpanded) {
                    reviewingWords.forEach { word ->
                        groupedItems.add(OverviewItem.WordItem(word))
                    }
                }
            }
            
            // 分组：已隐藏的单词（如果有的话）
            val hiddenWords = allWords.filter { it.getIsHidden() }
            if (hiddenWords.isNotEmpty()) {
                val isExpanded = expandedStates.getOrDefault("已隐藏的单词", true)
                groupedItems.add(OverviewItem.Header("已隐藏的单词", hiddenWords.size, isExpanded))
                if (isExpanded) {
                    hiddenWords.forEach { word ->
                        groupedItems.add(OverviewItem.WordItem(word))
                    }
                }
            }
        }
        
        items = groupedItems
        notifyDataSetChanged()
    }
    
    private var allWords: List<Word> = emptyList()
}