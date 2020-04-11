package com.bardia.pocr.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class TextObjectDecoded implements Parcelable {

    String text, date;
    Bitmap image;

    public TextObjectDecoded(String text, String date, Bitmap image) {
        this.text = text;
        this.date = date;
        this.image = image;
    }

    protected TextObjectDecoded(Parcel in) {
        text = in.readString();
        date = in.readString();
        image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<TextObjectDecoded> CREATOR = new Creator<TextObjectDecoded>() {
        @Override
        public TextObjectDecoded createFromParcel(Parcel in) {
            return new TextObjectDecoded(in);
        }

        @Override
        public TextObjectDecoded[] newArray(int size) {
            return new TextObjectDecoded[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "TextObjectDecoded{" +
                "text='" + text + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(date);
        parcel.writeParcelable(image, i);
    }
}
