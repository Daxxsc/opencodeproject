package com.example.words.data;

import androidx.room.*;
import java.util.Date;
import java.util.List;

@Dao
public interface WordDao {
    
    @Insert
    long insert(Word word);
    
    @Insert
    void insertAll(Word... words);
    
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
    
    @Query("SELECT * FROM words WHERE status = 'REVIEWING' ORDER BY nextReviewDate")
    List<Word> getWordsDueForReview();
    
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
    
    @Query("SELECT COUNT(*) FROM words WHERE status = 'REVIEWING'")
    int getDueReviewCount();
    
    @Query("SELECT COUNT(*) FROM words")
    int getWordCount();
    
    @Query("SELECT * FROM words WHERE status = 'REVIEWING' ORDER BY nextReviewDate")
    List<Word> getAllReviewWords();
    
    @Query("SELECT * FROM words WHERE status = 'REVIEWING' ORDER BY nextReviewDate LIMIT 1")
    Word getNextReviewWord();
    
    @Query("SELECT * FROM words WHERE id = :id")
    Word getWordById(long id);
}