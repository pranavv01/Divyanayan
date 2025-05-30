package com.example.divyanayan20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Uploadfragment extends Fragment implements FileListAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private float y1, y2;
    private static final int MIN_DISTANCE = 150;

    private final String clickedDir = "Divyanayan Clicked Pictures";
    private final String deviceDir = "Divyanayan Device Pictures";

    private boolean isShowingDirectories = true;

    private final ActivityResultLauncher<Intent> storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (isStoragePermissionGranted()) {
                    showDirectories();
                } else {
                    Toast.makeText(getContext(), "Storage permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uploadfragment, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (isStoragePermissionGranted()) {
            showDirectories();
        } else {
            requestStoragePermission();
        }

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    y1 = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                    y2 = event.getY();
                    float deltaY = y2 - y1;
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        if (deltaY > 0) {
                            showFilesFrom(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + clickedDir);
                        } else {
                            showFilesFrom(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + deviceDir);
                        }
                    }
                    return true;
            }
            return false;
        });

        return view;
    }

    private void showDirectories() {
        isShowingDirectories = true;
        List<String> directories = new ArrayList<>();
        directories.add(clickedDir);
        directories.add(deviceDir);

        recyclerView.setAdapter(new FileListAdapter(directories, this, true)); // true = directory mode
    }

    private void showFilesFrom(String folderPath) {
        isShowingDirectories = false;

        File directory = new File(folderPath);
        List<String> filePaths = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        filePaths.add(file.getAbsolutePath());
                    }
                }
            } else {
                Toast.makeText(getContext(), "No files found in: " + folderPath, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Folder not found: " + folderPath, Toast.LENGTH_SHORT).show();
        }

        recyclerView.setAdapter(new FileListAdapter(filePaths, this, false)); // false = file mode
    }

    @Override
    public void onItemClick(String itemPathOrName) {
        if (isShowingDirectories) {
            // User clicked on folder name, open files inside
            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + itemPathOrName;
            showFilesFrom(fullPath);
        } else {
            // User clicked on image, perform OCR
            recognizeTextFromImage(itemPathOrName);
        }
    }

    private void recognizeTextFromImage(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String extractedText = text.getText();
                    openExtractTextActivity(extractedText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to extract text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UploadFragment", "Text recognition failed", e);
                });
    }

    private void openExtractTextActivity(String extractedText) {
        Intent intent = new Intent(getContext(), ExtractTextActivity.class);
        intent.putExtra("EXTRACTED_TEXT", extractedText);
        startActivity(intent);
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                storagePermissionLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storagePermissionLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    101
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDirectories();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
