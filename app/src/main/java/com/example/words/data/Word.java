package com.example.words.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "words")
public class Word {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    
    public String english;
    public String chinese;
    public String example = "";
    public String phonetic = "";
    public String status = "UNKNOWN";
    public int reviewStage = 0;
    public Date nextReviewDate;
    public Date lastReviewedDate;
    public boolean isHidden = false;
    public Date createdAt = new Date();
    
    public Word() {}
    
    @Ignore
    public Word(String english, String chinese) {
        this.english = english;
        this.chinese = chinese;
    }
    
    // Getter for isHidden for Kotlin compatibility
    public boolean getIsHidden() {
        return isHidden;
    }
    
    // Setter for isHidden
    public void setIsHidden(boolean hidden) {
        isHidden = hidden;
    }
}