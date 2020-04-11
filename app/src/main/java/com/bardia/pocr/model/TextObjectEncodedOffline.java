package com.bardia.pocr.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "object")
public class TextObjectEncodedOffline {

    @NonNull
    @PrimaryKey(autoGenerate = false)
    public String nodeId;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "image")
    public String image;

    @ColumnInfo(name = "date")
    public String date;

    public TextObjectEncodedOffline(String nodeId, String text, String image, String date) {
        this.nodeId = nodeId;
        this.text = text;
        this.image = image;
        this.date = date;
    }

    @Override
    public String toString() {
        return "TextObjectEncodedOffline{" +
                "nodeId='" + nodeId + '\'' +
                ", text='" + text + '\'' +
                ", image='" + image.substring(0, 50) + "[...]" + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
