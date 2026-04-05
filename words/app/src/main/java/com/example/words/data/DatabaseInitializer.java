package com.example.words.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private static final String DEFAULT_WORDS_CSV_FILE = "kaoyan_words_100.csv";
    private static final String CUSTOM_WORDS_CSV_FILE = "my_words.csv"; // 您的自定义单词书
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static void initializeDatabase(@NonNull Context context) {
        executor.execute(() -> {
            WordDatabase database = WordDatabase.getDatabase(context);
            WordDao wordDao = database.wordDao();
            
            // 检查数据库中是否已有单词
            int wordCount = wordDao.getWordCount();
            if (wordCount > 0) {
                Log.i(TAG, "Database already contains " + wordCount + " words, skipping initialization");
                return;
            }
            
            List<Word> allWords = new ArrayList<>();
            
            // 首先尝试加载自定义单词书
            List<Word> customWords = loadWordsFromCSV(context, CUSTOM_WORDS_CSV_FILE);
            if (customWords != null && !customWords.isEmpty()) {
                allWords.addAll(customWords);
                Log.i(TAG, "Loaded " + customWords.size() + " words from custom CSV: " + CUSTOM_WORDS_CSV_FILE);
            } else {
                // 如果自定义文件不存在，加载默认单词书
                Log.i(TAG, "Custom CSV file not found, loading default words");
                List<Word> defaultWords = loadWordsFromCSV(context, DEFAULT_WORDS_CSV_FILE);
                if (defaultWords != null && !defaultWords.isEmpty()) {
                    allWords.addAll(defaultWords);
                    Log.i(TAG, "Loaded " + defaultWords.size() + " words from default CSV: " + DEFAULT_WORDS_CSV_FILE);
                }
            }
            
            if (!allWords.isEmpty()) {
                try {
                    wordDao.insertAll(allWords.toArray(new Word[0]));
                    Log.i(TAG, "Successfully imported " + allWords.size() + " words total");
                } catch (Exception e) {
                    Log.e(TAG, "Error inserting words into database", e);
                }
            } else {
                Log.w(TAG, "No words loaded from any CSV file");
            }
        });
    }
    
    private static List<Word> loadWordsFromCSV(@NonNull Context context, String csvFileName) {
        List<Word> words = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        
        try (InputStream inputStream = assetManager.open(csvFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 解析CSV行
                String[] parts = parseCSVLine(line);
                if (parts.length >= 2) {
                    String english = parts[0].trim();
                    String chinese = parts[1].trim();
                    
                    Word word = new Word(english, chinese);
                    word.status = "UNKNOWN"; // 初始状态为未知
                    word.reviewStage = 0;
                    word.nextReviewDate = null;
                    word.lastReviewedDate = null;
                    word.isHidden = false;
                    
                    words.add(word);
                }
            }
            
            Log.i(TAG, "Loaded " + words.size() + " words from CSV file: " + csvFileName);
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file: " + csvFileName, e);
            return null; // 文件不存在时返回null
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV file: " + csvFileName, e);
            return null;
        }
        
        return words;
    }
    
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}