package com.example.yogiapp.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraManager {

    private static final String TAG = "CameraManager";

    private Context context;
    private PreviewView previewView;
    private ImageView photoPreview;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private Executor executor;

    public interface SnapshotCallback {
        void onSnapshotCaptured(File imageFile, Bitmap bitmap);
    }

    public CameraManager(Context context, PreviewView previewView, ImageView photoPreview) {
        this.context = context;
        this.previewView = previewView;
        this.photoPreview = photoPreview;
        executor = ContextCompat.getMainExecutor(context);
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, executor);
    }

    private void bindCameraUseCases() {
        imageCapture = new ImageCapture.Builder().build();

        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(
                    (androidx.lifecycle.LifecycleOwner) context,
                    cameraSelector,
                    preview,
                    imageCapture
            );
        }
    }

    public void captureSnapshot(final SnapshotCallback callback) {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture not initialized");
            return;
        }
        File photoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                callback.onSnapshotCaptured(photoFile, bitmap);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Image capture failed", exception);
            }
        });
    }

    public void shutdown() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
