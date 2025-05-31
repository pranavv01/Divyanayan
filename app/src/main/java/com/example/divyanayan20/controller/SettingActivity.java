package com.example.divyanayan20.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;

import com.example.divyanayan20.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class SettingActivity extends AppCompatActivity {
    private RadioGroup ocrEngineGroup;
    private SharedPreferences sharedPreferences;
    private static final String APP_NAME = "Divyanayan20";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ocrEngineGroup = findViewById(R.id.ocrEngineGroup);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Load saved selection
        String selectedEngine = sharedPreferences.getString("OCR_ENGINE", "MLKit");
        if (selectedEngine.equals("Tesseract")) {
            ((RadioButton) findViewById(R.id.radioTesseract)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radioMLKit)).setChecked(true);
        }

        // Save selected option
        ocrEngineGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String engine = checkedId == R.id.radioTesseract ? "Tesseract" : "MLKit";
            sharedPreferences.edit().putString("OCR_ENGINE", engine).apply();
        });

    }
}
