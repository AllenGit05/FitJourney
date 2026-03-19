# FitJourney Android App

FitJourney is a comprehensive AI-powered fitness and nutrition companion designed to help you achieve your health goals.

## Getting Started

To build and run the project locally, you must provide your own Firebase configuration.

### Prerequisites

- Android Studio Koala+
- A Firebase Project

### Setup Instructions

1.  **Firebase Configuration**:
    - Go to the [Firebase Console](https://console.firebase.google.com/).
    - Create a new project and add an Android app with the package name `com.example.fitjourney`.
    - Download the `google-services.json` file.
    - Place the `google-services.json` file in the `app/` directory of your local repository.
    - Note: `app/google-services.json` is ignored by Git for security. See `app/google-services.json.example` for the expected structure.

2.  **API Key Security (CRITICAL)**:
    - **Rotate Keys**: Since the previous API key was exposed, you MUST rotate your Firebase API key in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
    - **Restrict Keys**: Restrict your new API key to the Android package name (`com.example.fitjourney`) and your developer SHA-1 certificate fingerprint.
    - **App Check**: Enable **Firebase App Check** in the Firebase Console to protect your backend resources from abuse.

3.  **Build**:
    - Sync the project with Gradle files.
    - Build and run on an emulator or physical device.

## Security Note

Never commit sensitive information like API keys or configuration files directly to the repository. Always use environment variables or local ignored files (like `google-services.json` and `local.properties`).
