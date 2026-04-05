package com.example.words.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        // 数据库创建时初始化数据
                        DatabaseInitializer.initializeDatabase(context.getApplicationContext());
                    }
                    
                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        // 数据库打开时检查是否需要初始化
                        DatabaseInitializer.initializeDatabase(context.getApplicationContext());
                    }
                })
                .build();
                }
            }
        }
        return INSTANCE;
    }
}