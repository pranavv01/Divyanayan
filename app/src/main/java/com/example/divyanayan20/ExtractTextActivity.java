package com.example.divyanayan20;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ExtractTextActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private String extractedText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract_text);

        TextView extractedTextView = findViewById(R.id.extractedTextView);
        Button ttsButton = findViewById(R.id.ttsButton); // Add a button to trigger TTS

        // Get the text passed from ReadFragment
        extractedText = getIntent().getStringExtra("EXTRACTED_TEXT");

        if (extractedText != null) {
            extractedTextView.setText(extractedText);
        }

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // Set click listener to read the extracted text aloud
        ttsButton.setOnClickListener(v -> speakText());
    }

    private void speakText() {
        if (textToSpeech != null && extractedText != null) {
            textToSpeech.speak(extractedText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
