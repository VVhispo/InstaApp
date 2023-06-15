package com.example.instaapp.model;

import android.graphics.Bitmap;

public final class BitmapData {
    private static final BitmapData bitmapData = new BitmapData();
    private Bitmap bitmap;

    private BitmapData() {
    }

    public static BitmapData getInstance() {
        return bitmapData;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}