package com.bardia.pocr.model;

public class TextObjectEncoded {

    private String text, image, date;

    public TextObjectEncoded() {
    }

    public TextObjectEncoded(String text, String image, String date) {
        this.text = text;
        this.image = image;
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TextObjectEncoded{" +
                "text='" + text + '\'' +
                ", image='" + image + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
