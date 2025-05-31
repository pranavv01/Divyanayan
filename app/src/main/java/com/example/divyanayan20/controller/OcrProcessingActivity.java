package com.example.divyanayan20.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.divyanayan20.R;

public class OcrProcessingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_processing);

        TextView resultTextView = findViewById(R.id.resultTextView);

        // Get the extracted text from intent
        String extractedText = getIntent().getStringExtra("extracted_text");
        if (extractedText != null && !extractedText.isEmpty()) {
            resultTextView.setText(extractedText);
        } else {
            resultTextView.setText("No text extracted.");
        }
    }
}
