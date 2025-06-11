package com.example.birdclassifier;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_RECORD_AUDIO = 2033;
    String modelPath = "yamnet_classification.tflite";
    float probabilityThreshold = 0.3f;
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;
    protected TextView outputTextView;
    protected Button startRecordingButton;
    protected Button stopRecordingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputTextView = findViewById(R.id.textViewOutput);
        startRecordingButton = findViewById(R.id.buttonStartRecording);
        stopRecordingButton = findViewById(R.id.buttonStopRecording);

        stopRecordingButton.setEnabled(false);

        if (checkRecordAudioPermission()) {
            startRecordingButton.setEnabled(true);
        }

        onStartRecording(startRecordingButton);
        onStopRecording(stopRecordingButton);
    }

    private void onStopRecording(Button stopButton) {
        stopButton.setOnClickListener(view -> {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
            
            if (timerTask != null) {
                timerTask.cancel();
            }
            
            if (record != null) {
                record.stop();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void onStartRecording(Button startButton) {
        startButton.setOnClickListener(view -> {
            startRecordingButton.setEnabled(false);
            stopRecordingButton.setEnabled(true);
            showToast("Recording started");

            try {
                classifier = AudioClassifier.createFromFile(this, modelPath);
                showToast("Model loaded successfully");
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error loading model: " + e.getMessage());
                return;
            }

            tensor = classifier.createInputTensorAudio();

            // Get and show audio format requirements
            TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
            String specs = "Audio specs - Channels: " + format.getChannels() + 
                         ", Sample Rate: " + format.getSampleRate();
            outputTextView.setText(specs);

            record = classifier.createAudioRecord();
            record.startRecording();

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    tensor.load(record);
                    List<Classifications> output = classifier.classify(tensor);
                    List<Category> finalOutput = new ArrayList<>();

                    // Process all classifications
                    for (Classifications classifications : output) {
                        for (Category category : classifications.getCategories()) {
                            if (category.getScore() > probabilityThreshold) {
                                finalOutput.add(category);
                            }
                        }
                    }

                    // Sort by score in descending order
                    Collections.sort(finalOutput, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));

                    StringBuilder outputStr = new StringBuilder();
                    
                    // Show top 5 predictions with formatted scores
                    int count = 0;
                    for (Category category : finalOutput) {
                        if (count++ < 5) {
                            outputStr.append(category.getLabel())
                                   .append(": ")
                                   .append(String.format("%.2f", category.getScore()))
                                   .append("\n");
                        }
                    }

                    runOnUiThread(() -> {
                        if (finalOutput.isEmpty()) {
                            outputTextView.setText("Listening for sounds...\nMake sure there's enough volume");
                        } else {
                            outputTextView.setText(outputStr.toString());
                        }
                    });
                }
            };

            new Timer().scheduleAtFixedRate(timerTask, 1, 500);
        });
    }

    private boolean checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, 
                    REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }
}
