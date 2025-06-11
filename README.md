# Bird Sound Classifier

A simple Android app that detects bird sounds using TensorFlow Lite.

## Features

- Real-time bird sound detection using YAMNet model
- Simple user interface with Start/Stop recording
- Shows detection confidence percentage

## Project Structure

1. `notebooks/` - Contains the training notebook for bird species classifier
2. `app/` - Android application source code

## Setup Instructions

### Training the Model

1. Open `notebooks/bird_classifier.ipynb` in Google Colab or Jupyter Notebook
2. Update the `DATA_DIR` path to point to your bird sound dataset
3. Run all cells to train the model
4. The trained model will be saved as `bird_classifier.tflite`

### Running the Android App

1. Open the project in Android Studio
2. Copy the YAMNet model to `app/src/main/assets/`
3. Build and run the app on your device

## Requirements

- Android Studio
- Android device with minimum SDK 24 (Android 7.0)
- Python environment for training (if using the classifier notebook)

## Usage

1. Launch the app
2. Grant microphone permission when prompted
3. Press "Start" to begin recording
4. The app will show "Bird detected!" with confidence score when it detects bird sounds
5. Press "Stop" to end recording
