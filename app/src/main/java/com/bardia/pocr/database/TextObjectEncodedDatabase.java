package com.bardia.pocr.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.bardia.pocr.dao.TextObjectEncodedDAO;
import com.bardia.pocr.model.TextObjectEncoded;
import com.bardia.pocr.model.TextObjectEncodedOffline;

@Database(entities = TextObjectEncodedOffline.class, exportSchema = false, version = 1)
public abstract class TextObjectEncodedDatabase extends RoomDatabase {
    private static final String DB_NAME = "offline_database";
    private static TextObjectEncodedDatabase instance;

    public static synchronized TextObjectEncodedDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), TextObjectEncodedDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
        }
        return instance;
    }

    public abstract TextObjectEncodedDAO textObjectEncodedDAO();
}
