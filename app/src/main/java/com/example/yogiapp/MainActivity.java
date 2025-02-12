package com.example.yogiapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogiapp.controllers.ChatGPTController;
import com.example.yogiapp.managers.CameraManager;
import com.example.yogiapp.managers.SpeechRecognitionManager;
import com.example.yogiapp.models.ChatMessage;
import com.example.yogiapp.ui.ChatAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SpeechRecognitionManager.OnCommandListener {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final String TAG = "MainActivity";

    private PreviewView videoPreview;
    private ImageView photoPreview;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;

    private SpeechRecognitionManager speechManager;
    private CameraManager cameraManager;
    private ChatGPTController chatGPTController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         // Check for required permissions.
         if (!hasPermissions()) {
             ActivityCompat.requestPermissions(this,
                     new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                     REQUEST_PERMISSIONS);
         }

         // Initialize views.
         videoPreview = findViewById(R.id.video_preview);
         photoPreview = findViewById(R.id.photoPreview);
         chatRecyclerView = findViewById(R.id.chatRecyclerView);
         chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
         chatAdapter = new ChatAdapter();
         chatRecyclerView.setAdapter(chatAdapter);

         // Initialize managers.
         cameraManager = new CameraManager(this, videoPreview, photoPreview);
         cameraManager.startCamera(); // Start CameraX preview.
         chatGPTController = new ChatGPTController(this);
         speechManager = new SpeechRecognitionManager(this, this);
         speechManager.startListening();
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCommandReceived(String command) {
        // Add the command to the chat history.
        chatAdapter.addMessage(new ChatMessage(ChatMessage.Type.REQUEST, command));
        Log.d(TAG, "Received command: " + command);

        // Send the command to ChatGPT so it can decide which action to take.
        chatGPTController.callChatGPT(command, new ChatGPTController.ChatGPTCallback() {
            @Override
            public void onSuccess(final String jsonResponse) {
                Log.d(TAG, "ChatGPT response: " + jsonResponse);
                runOnUiThread(() -> {
                    // Add the ChatGPT response to the chat history.
                    chatAdapter.addMessage(new ChatMessage(ChatMessage.Type.RESPONSE, jsonResponse));

                    // Parse the returned JSON to extract action and parameters.
                    try {
                        JSONObject responseObj = new JSONObject(jsonResponse);
                        String action = responseObj.getString("action");
                        JSONObject params = responseObj.optJSONObject("params");

                        // Decide which API to call based on the action.
                        switch (action) {
                            case "capture_snapshot":
                                cameraManager.captureSnapshot((imageFile, bitmap) -> {
                                    runOnUiThread(() -> {
                                        photoPreview.setImageBitmap(bitmap);
                                        Toast.makeText(MainActivity.this, "Snapshot captured", Toast.LENGTH_SHORT).show();
                                    });
                                });
                                break;
                            case "start_video":
                                Toast.makeText(MainActivity.this, "Starting video...", Toast.LENGTH_SHORT).show();
                                break;
                            case "stop_video":
                                Toast.makeText(MainActivity.this, "Stopping video...", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(MainActivity.this, "Unknown action: " + action, Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error parsing ChatGPT response", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "JSON parse error", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error calling ChatGPT", e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechManager.destroy();
        cameraManager.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (!hasPermissions()) {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
