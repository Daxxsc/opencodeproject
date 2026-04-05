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
    private static final String WORDS_CSV_FILE = "kaoyan_words_100.csv";
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
            
            // 从CSV文件导入单词
            List<Word> words = loadWordsFromCSV(context);
            if (words != null && !words.isEmpty()) {
                try {
                    wordDao.insertAll(words.toArray(new Word[0]));
                    Log.i(TAG, "Successfully imported " + words.size() + " words from CSV");
                } catch (Exception e) {
                    Log.e(TAG, "Error inserting words into database", e);
                }
            }
        });
    }
    
    private static List<Word> loadWordsFromCSV(@NonNull Context context) {
        List<Word> words = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        
        try (InputStream inputStream = assetManager.open(WORDS_CSV_FILE);
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
                    String phonetic = parts.length > 2 ? parts[2].trim() : "";
                    String example = parts.length > 3 ? parts[3].trim() : "";
                    
                    Word word = new Word(english, chinese);
                    word.phonetic = phonetic;
                    word.example = example;
                    word.status = "UNKNOWN"; // 初始状态为未知
                    word.reviewStage = 0;
                    word.nextReviewDate = null;
                    word.lastReviewedDate = null;
                    word.isHidden = false;
                    
                    words.add(word);
                }
            }
            
            Log.i(TAG, "Loaded " + words.size() + " words from CSV file");
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file: " + WORDS_CSV_FILE, e);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing CSV file", e);
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