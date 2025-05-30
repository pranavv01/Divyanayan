package com.example.divyanayan20;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileID;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_IMAGE_URI = "image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        profileID = findViewById(R.id.profileID);

        // Load the saved image URI
        loadProfileImage();

        profileID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opengallery();
            }
        });
    }

    private void opengallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            profileID.setImageURI(imageUri);
                            saveImageUri(imageUri.toString());
                        }
                    }
                }
            }
    );

    private void saveImageUri(String uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IMAGE_URI, uri);
        editor.apply();
    }

    private void loadProfileImage() {
        String savedUri = sharedPreferences.getString(KEY_IMAGE_URI, null);
        if (savedUri != null) {
            profileID.setImageURI(Uri.parse(savedUri));
        }
    }
}