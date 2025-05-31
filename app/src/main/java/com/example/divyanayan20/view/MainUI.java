package com.example.divyanayan20.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;

import com.example.divyanayan20.controller.ContactusActivity;
import com.example.divyanayan20.controller.FAQsActivity;
import com.example.divyanayan20.controller.LanguageActivity;
import com.example.divyanayan20.controller.ProfileActivity;
import com.example.divyanayan20.R;
import com.example.divyanayan20.controller.ReadFragment;
import com.example.divyanayan20.controller.SettingActivity;
import com.example.divyanayan20.controller.TermsActivity;
import com.example.divyanayan20.controller.Uploadfragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MainUI extends AppCompatActivity {
    ImageView menubutton;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialButton btnread,btnupload;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);

        drawerLayout = findViewById(R.id.drawerlayout);
        menubutton = findViewById(R.id.menubutton);
        navigationView = findViewById(R.id.navigationview);
        btnread = findViewById(R.id.btnread);
//        btndocument = findViewById(R.id.btndocument);
        btnupload = findViewById(R.id.btnupload);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Load ReadFragment by default and set the default button style for btnread
        if (savedInstanceState == null) {
            replaceFragment(new ReadFragment());
            setSelectedButton(btnread);
        }

        // Button Click Listeners
        btnread.setOnClickListener(v -> {
            speak("Read");
            replaceFragment(new ReadFragment());
            setSelectedButton(btnread);
        });


        btnupload.setOnClickListener(v -> {
            speak("Upload");
            replaceFragment(new Uploadfragment());
            setSelectedButton(btnupload);
        });

        // Menu Button Click Listener
        menubutton.setOnClickListener(v -> {
            speak("Menu");
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Navigation Drawer Listener with TTS
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemid = item.getItemId();
            String itemName = item.getTitle().toString();
            speak(itemName);

            if (itemid == R.id.navProfile) {
                startActivity(new Intent(MainUI.this, ProfileActivity.class));
            } else if (itemid == R.id.navsetting) {
                startActivity(new Intent(MainUI.this, SettingActivity.class));
            } else if (itemid == R.id.navLang) {
                startActivity(new Intent(MainUI.this, LanguageActivity.class));
            } else if (itemid == R.id.navFAQs) {
                startActivity(new Intent(MainUI.this, FAQsActivity.class));
            } else if (itemid == R.id.navTerms) {
                startActivity(new Intent(MainUI.this, TermsActivity.class));
            } else if (itemid == R.id.navContact) {
                startActivity(new Intent(MainUI.this, ContactusActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Set selected button style and reset others
    private void setSelectedButton(MaterialButton selectedButton) {
        resetButtonStyles();
        int selectedBgColor = ContextCompat.getColor(this, android.R.color.black);
        int selectedTextColor = ContextCompat.getColor(this, android.R.color.white);
        selectedButton.setBackgroundTintList(ColorStateList.valueOf(selectedBgColor));
        selectedButton.setTextColor(selectedTextColor);
    }

    // Reset all buttons to default style
    private void resetButtonStyles() {
        int defaultBgColor = ContextCompat.getColor(this, android.R.color.white);
        int defaultTextColor = ContextCompat.getColor(this, android.R.color.black);

        btnread.setBackgroundTintList(ColorStateList.valueOf(defaultBgColor));
        btnread.setTextColor(defaultTextColor);
//
//        btndocument.setBackgroundTintList(ColorStateList.valueOf(defaultBgColor));
//        btndocument.setTextColor(defaultTextColor);

        btnupload.setBackgroundTintList(ColorStateList.valueOf(defaultBgColor));
        btnupload.setTextColor(defaultTextColor);
    }

    // Speak function for TTS
    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Replace the current fragment
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
