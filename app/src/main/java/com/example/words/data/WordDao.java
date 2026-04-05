package com.example.words.data;

import androidx.room.*;
import java.util.Date;
import java.util.List;

@Dao
public interface WordDao {
    
    @Insert
    long insert(Word word);
    
    @Update
    void update(Word word);
    
    @Delete
    void delete(Word word);
    
    @Query("SELECT * FROM words ORDER BY id")
    List<Word> getAllWords();
    
    @Query("SELECT * FROM words WHERE status = :status ORDER BY id")
    List<Word> getWordsByStatus(String status);
    
    @Query("SELECT * FROM words WHERE isHidden = :hidden ORDER BY id")
    List<Word> getWordsByHiddenStatus(boolean hidden);
    
    @Query("SELECT * FROM words WHERE status = 'REVIEWING' AND nextReviewDate <= :currentDate ORDER BY nextReviewDate")
    List<Word> getWordsDueForReview(Date currentDate);
    
    @Query("SELECT * FROM words WHERE status = 'UNKNOWN' ORDER BY RANDOM() LIMIT 1")
    Word getRandomUnknownWord();
    
    @Query("SELECT COUNT(*) FROM words WHERE status = 'KNOWN'")
    int getKnownCount();
    
    @Query("SELECT COUNT(*) FROM words WHERE status = 'UNKNOWN'")
    int getUnknownCount();
    
    @Query("SELECT COUNT(*) FROM words WHERE status = 'REVIEWING'")
    int getReviewingCount();
    
    @Query("SELECT COUNT(*) FROM words WHERE isHidden = 1")
    int getHiddenCount();
    
    @Query("SELECT COUNT(*) FROM words WHERE status = 'REVIEWING' AND nextReviewDate <= :currentDate")
    int getDueReviewCount(Date currentDate);
    
    @Query("SELECT COUNT(*) FROM words")
    int getWordCount();
    
    @Query("SELECT * FROM words WHERE status = 'REVIEWING' AND nextReviewDate <= :currentDate ORDER BY nextReviewDate LIMIT 1")
    Word getNextReviewWord(Date currentDate);
}