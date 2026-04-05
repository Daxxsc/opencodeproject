package com.example.words.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
    entities = {Word.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class WordDatabase extends RoomDatabase {
    
    public abstract WordDao wordDao();
    
    private static volatile WordDatabase INSTANCE;
    
    public static WordDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WordDatabase.class) {
                if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    WordDatabase.class,
                    "word_database"
                ).allowMainThreadQueries()  // 允许在主线程查询，仅用于调试
                .build();
                }
            }
        }
        return INSTANCE;
    }
}