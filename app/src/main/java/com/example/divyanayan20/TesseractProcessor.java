// TesseractProcessor.java
package com.example.divyanayan20;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TesseractProcessor {
    private final TessBaseAPI tessBaseAPI;

    public TesseractProcessor(Context context, String lang) {
        tessBaseAPI = new TessBaseAPI();
        String datapath = context.getFilesDir() + "/tesseract/";
        if (tessBaseAPI.init(datapath, lang)) {
            Log.d("Tesseract", "Tesseract initialized");
        } else {
            Log.e("Tesseract", "Could not initialize Tesseract.");
        }
    }

    public String extractText(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void shutdown() {
        tessBaseAPI.recycle();
    }
}
