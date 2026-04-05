package com.example.words.ui.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.words.R
import com.example.words.data.Word

class WordAdapter(
    private var words: List<Word> = emptyList(),
    private val onWordClick: (Word) -> Unit = {}
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }
}