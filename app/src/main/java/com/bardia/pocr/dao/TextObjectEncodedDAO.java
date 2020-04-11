package com.bardia.pocr.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bardia.pocr.model.TextObjectEncodedOffline;

import java.util.List;

@Dao
public interface TextObjectEncodedDAO {

    @Query("Select * from object")
    List<TextObjectEncodedOffline> getEncodedObjectsList();

    @Query("Select * from object where nodeId = :nodeId")
    TextObjectEncodedOffline getEncodedObject(String nodeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEncodedObject(TextObjectEncodedOffline objectEncodedOffline);

    @Update
    void updateObjectEncoded(TextObjectEncodedOffline objectEncodedOffline);

    @Delete
    void deleteObjectEncoded(TextObjectEncodedOffline objectEncodedOffline);

    @Query("Delete from object")
    void deleteAllObjects();
}
