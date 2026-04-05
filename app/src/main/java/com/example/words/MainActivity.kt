package com.example.words

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.words.data.WordDatabase
import com.example.words.databinding.ActivityMainBinding
import com.example.words.ui.learning.LearningFragment
import com.example.words.ui.overview.OverviewFragment
import com.example.words.ui.review.ReviewFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // 初始化数据库（这会触发单词库导入）
            WordDatabase.getDatabase(applicationContext)
            
            setupBottomNavigation()
            
            if (savedInstanceState == null) {
                navigateToFragment(LearningFragment())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_learning -> {
                    navigateToFragment(LearningFragment())
                    true
                }
                R.id.nav_review -> {
                    navigateToFragment(ReviewFragment())
                    true
                }
                R.id.nav_overview -> {
                    navigateToFragment(OverviewFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}