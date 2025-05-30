package com.example.divyanayan20;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadFragment extends Fragment {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextRecognizer selectedRecognizer;
    private boolean usingMLKit = true;

    private final String[] languageNames = {"Latin", "Chinese", "Devanagari", "Japanese", "Korean"};
    private int languageIndex = 0;

    private int downwardSwipes = 0;
    private boolean isWaitingForSecondSwipe = false;
    private Runnable swipeTimeoutRunnable;
    private Runnable beepRunnable;
    private int beepCount = 0;

    private int upwardSwipes = 0;
    private boolean isWaitingForSecondUpSwipe = false;
    private Runnable upSwipeTimeoutRunnable;
    private Runnable upBeepRunnable;
    private int upBeepCount = 0;
    private Handler swipeHandler = new Handler();
    private TessBaseAPI tessBaseAPI;
    private final String tessDataPath = "/data/user/0/com.example.divyanayan20/files/tesseract/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
        updateRecognizerByIndex(languageIndex);
        initializeTesseract("eng");

        GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (diffY > 100) {
                        handleDownwardSwipe();
                    } else if (diffY < -100) {
                        handleUpwardSwipe();
                    }
                } else {
                    if (diffX > 100) {
                        languageIndex = (languageIndex + 1) % languageNames.length;
                        updateRecognizerByIndex(languageIndex);
                    } else if (diffX < -100) {
                        languageIndex = (languageIndex - 1 + languageNames.length) % languageNames.length;
                        updateRecognizerByIndex(languageIndex);
                    }
                }
                return true;
            }
        });

        previewView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        return view;
    }

    private void updateRecognizerByIndex(int index) {
        if (!usingMLKit) return;

        switch (index) {
            case 0:
                selectedRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                break;
            case 1:
                selectedRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
                break;
            case 2:
                selectedRecognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
                break;
            case 3:
                selectedRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
                break;
            case 4:
                selectedRecognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
                break;
        }
        Toast.makeText(getContext(), "Language: " + languageNames[index], Toast.LENGTH_SHORT).show();
    }

    private void initializeTesseract(String langCode) {
        new File(tessDataPath + "tessdata/").mkdirs();  // Make sure the tessdata folder exists
        try {
            File trainedData = new File(tessDataPath + "tessdata/" + langCode + ".traineddata");
            if (!trainedData.exists()) {
                InputStream in = requireContext().getAssets().open("tessdata/" + langCode + ".traineddata");
                OutputStream out = new FileOutputStream(trainedData);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(tessDataPath, langCode);
    }


    private void handleDownwardSwipe() {
        if (!isWaitingForSecondSwipe) {
            downwardSwipes = 1;
            isWaitingForSecondSwipe = true;
            startBeepCountdown();

            swipeTimeoutRunnable = () -> {
                downwardSwipes = 0;
                isWaitingForSecondSwipe = false;
                swipeHandler.removeCallbacks(beepRunnable);
            };
            swipeHandler.postDelayed(swipeTimeoutRunnable, 5000);
        } else {
            downwardSwipes++;
            if (downwardSwipes == 2) {
                swipeHandler.removeCallbacks(swipeTimeoutRunnable);
                swipeHandler.removeCallbacks(beepRunnable);
                isWaitingForSecondSwipe = false;
                downwardSwipes = 0;
                vibrate();
                captureImage();
            }
        }
    }

    private void handleUpwardSwipe() {
        if (!isWaitingForSecondUpSwipe) {
            upwardSwipes = 1;
            isWaitingForSecondUpSwipe = true;
            startUpBeepCountdown();

            upSwipeTimeoutRunnable = () -> {
                upwardSwipes = 0;
                isWaitingForSecondUpSwipe = false;
                swipeHandler.removeCallbacks(upBeepRunnable);
            };
            swipeHandler.postDelayed(upSwipeTimeoutRunnable, 3000);
        } else {
            upwardSwipes++;
            if (upwardSwipes == 2) {
                swipeHandler.removeCallbacks(upSwipeTimeoutRunnable);
                swipeHandler.removeCallbacks(upBeepRunnable);
                isWaitingForSecondUpSwipe = false;
                upwardSwipes = 0;
                toggleOCREngine();
            }
        }
    }

    private void toggleOCREngine() {
        usingMLKit = !usingMLKit;
        String engine = usingMLKit ? "Google ML Kit" : "Tesseract";
        Toast.makeText(getContext(), "Engine: " + engine, Toast.LENGTH_SHORT).show();

        if (usingMLKit) {
            updateRecognizerByIndex(languageIndex);
        } else {
            initializeTesseract("eng");
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Camera initialization failed", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(getContext(), "Image capture not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                processImage(image);
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getContext(), "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processImage(ImageProxy imageProxy) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Extracting text...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (usingMLKit && selectedRecognizer != null) {
            try {
                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                selectedRecognizer.process(image)
                        .addOnSuccessListener(text -> {
                            progressDialog.dismiss();
                            if (text.getText().isEmpty()) {
                                Toast.makeText(getContext(), "No text found", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(getActivity(), ExtractTextActivity.class);
                                intent.putExtra("EXTRACTED_TEXT", text.getText());
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Text recognition failed", Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e("MLKit", "Image processing error", e);
            }
        } else {
            Bitmap bitmap = imageProxyToBitmap(imageProxy);
            new Thread(() -> {
                tessBaseAPI.setImage(bitmap);
                String result = tessBaseAPI.getUTF8Text();
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (result.isEmpty()) {
                        Toast.makeText(getContext(), "No text found", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getActivity(), ExtractTextActivity.class);
                        intent.putExtra("EXTRACTED_TEXT", result);
                        startActivity(intent);
                    }
                });
            }).start();
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void startBeepCountdown() {
        beepCount = 0;
        beepRunnable = new Runnable() {
            @Override
            public void run() {
                beep();
                beepCount++;
                if (beepCount < 5) {
                    swipeHandler.postDelayed(this, 1000);
                }
            }
        };
        swipeHandler.post(beepRunnable);
    }

    private void startUpBeepCountdown() {
        upBeepCount = 0;
        upBeepRunnable = new Runnable() {
            @Override
            public void run() {
                beep();
                upBeepCount++;
                if (upBeepCount < 3) {
                    swipeHandler.postDelayed(this, 1000);
                }
            }
        };
        swipeHandler.post(upBeepRunnable);
    }

    private void beep() {
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        swipeHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}