# Divyanayan

Divyanayan is an Android app designed to assist users by capturing images using the camera, extracting text using Optical Character Recognition (OCR), and reading the extracted text aloud. The app supports multiple languages and allows toggling between two OCR engines: Google ML Kit and Tesseract OCR. It also integrates Google Sign-In and Firebase Authentication for user login.

---

## Features

- **Real-time Camera Preview & Image Capture** using Android CameraX API.
- **OCR Support** for multiple languages including Latin, Chinese, Devanagari, Japanese, and Korean.
- Toggle between:
  - Google ML Kit OCR (cloud/local on-device)
  - Tesseract OCR (offline)
- **Gesture Controls:**
  - Swipe left/right to switch OCR languages.
  - Double downward swipe to capture image and extract text.
  - Double upward swipe to toggle OCR engines.
- **Text Extraction Display** in a dedicated activity.
- **Text-to-Speech (TTS)** for spoken feedback and reading extracted text.
- **Google Sign-In & Firebase Authentication** for secure user login.
- Runtime permissions handling for camera and audio.

---

## Networking Features

- **UDP for Device Discovery:** Enables discovering Divyanayan standalone devices over the local network.
- **TCP/IP for Data Transfer:** Allows sending images from a standalone device to the app over WiFi for OCR processing.
- This setup facilitates seamless connectivity and data exchange between Divyanayan devices and the Android app.

---

## Research & Development

- Conducted deep research on OCR engines including Google ML Kit and Tesseract OCR.
- Created a diverse multi-language dataset with various challenging parameters such as blurry images, dim lighting, and other real-world conditions.
- Performed extensive accuracy testing and comparative analysis of both OCR engines on this dataset.
- Integrated both OCR engines into the app based on research findings to provide flexible and accurate text recognition.
- This research forms the foundation of Divyanayan’s OCR capabilities.

---

## Tech Stack

- Android SDK  
- Java  
- CameraX API  
- Google ML Kit OCR  
- Tesseract OCR  
- Firebase Authentication  
- Android TextToSpeech API  
- Android GestureDetector and Lifecycle components  
- Networking with UDP & TCP/IP for device communication  

---

## Getting Started

### Prerequisites

- Android Studio 4.0 or higher  
- Android device or emulator with camera support  
- Firebase project with Google Sign-In enabled

### Installation

```bash
git clone https://github.com/yourusername/divyanayan.git
