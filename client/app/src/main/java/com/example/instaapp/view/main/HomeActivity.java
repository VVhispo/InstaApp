package com.example.instaapp.view.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.instaapp.R;
import com.example.instaapp.api.ImageAPI;
import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.ActivityHomeBinding;
import com.example.instaapp.model.BitmapData;
import com.example.instaapp.model.Photo;
import com.example.instaapp.view.photo.PhotoEditorActivity;
import com.example.instaapp.view.photo.VideoEditorActivity;
import com.example.instaapp.view.user_profile.UserActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mainBinding;
    private String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"};

    private int PERMISSIONS_REQUEST_CODE = 100;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
    private ProcessCameraProvider cameraProvider;
    private Boolean recording = false;
    private VideoCapture videoCapture;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);


        getProfilePic(mainBinding.profileBtn);
        mainBinding.photoBtn.setOnClickListener(v -> {
            takePhoto();
        });

        mainBinding.photoBtn.setOnLongClickListener(v -> {
            handler.postDelayed(new Runnable() {
                public void run() {
                    recording = true;
                    start_recording();
                    mainBinding.stopRecordingBtn.setOnClickListener(v -> stop_recording());
                }
            }, 500);
            return true;
        });
        mainBinding.switchCameraBtn.setOnClickListener(v -> switchCamera());
        mainBinding.profileBtn.setOnClickListener(v -> switchActivityToUser());

        if (!checkIfPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        } else {
            start_camera();
        }
    }

    @SuppressLint({"RestrictedApi", "MissingPermission"})
    public void start_recording() {
        mainBinding.profileBtn.setVisibility(View.INVISIBLE);
        mainBinding.stopRecordingBtn.setVisibility(View.VISIBLE);
        mainBinding.switchCameraBtn.setVisibility(View.INVISIBLE);
        View recSign = mainBinding.recordingSign;
        recSign.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            public void run() {
                if (recSign.getVisibility() == View.VISIBLE) recSign.setVisibility(View.INVISIBLE);
                else recSign.setVisibility(View.VISIBLE);
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        cameraProvider.unbindAll();
        bindPreview(cameraProvider);

        Date dNow = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        String datetime = ft.format(dNow);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, datetime);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");



        videoCapture.startRecording(
                new VideoCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                ContextCompat.getMainExecutor(this),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        Uri imgData = outputFileResults.getSavedUri();
                        Intent intent = new Intent(HomeActivity.this, VideoEditorActivity.class);
                        intent.putExtra("URI", imgData);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        // error
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    public void stop_recording(){
        videoCapture.stopRecording();
        mainBinding.profileBtn.setVisibility(View.VISIBLE);
        mainBinding.stopRecordingBtn.setVisibility(View.INVISIBLE);
        mainBinding.switchCameraBtn.setVisibility(View.VISIBLE);
        handler.removeCallbacksAndMessages(null);
        mainBinding.recordingSign.setVisibility(View.INVISIBLE);
        cameraProvider.unbindAll();
        bindPreview(cameraProvider);
    }

    public void switchActivityToUser(){
        Intent intent = new Intent(HomeActivity.this, UserActivity.class);
        startActivity(intent);
    }
    public void switchCamera(){
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
        cameraProvider.unbindAll();
        bindPreview(cameraProvider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        start_camera();
    }

    public void takePhoto(){
        Vibrator vibe = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibe.vibrate(100);
        if(recording){
            stop_recording();
            return;
        }
        Date dNow = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        String datetime = ft.format(dNow);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, datetime);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues)
                        .build();
        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(getBaseContext()),
                new ImageCapture.OnImageSavedCallback() {
                    Bitmap bm = mainBinding.camera.getBitmap();
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri imgData = outputFileResults.getSavedUri();
                        Intent intent = new Intent(HomeActivity.this, PhotoEditorActivity.class);
                        intent.putExtra("URI", imgData);
                        BitmapData.getInstance().setBitmap(bm);
                        startActivity(intent);
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) { Log.d("XXX", exception.toString()); }
                });

    }

    public void start_camera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(HomeActivity.this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (InterruptedException | ExecutionException e) {
                // No errors need to be handled for this Future. This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(mainBinding.camera.getDisplay().getRotation())
                        .setJpegQuality(75)
                        .build();

        preview.setSurfaceProvider(mainBinding.camera.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                        .setTargetRotation(mainBinding.camera.getDisplay().getRotation())
                        .build();

        if(recording) cameraProvider.bindToLifecycle(this, lensFacing, imageCapture, videoCapture, preview);
        else cameraProvider.bindToLifecycle(this, lensFacing, imageCapture, preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProfilePic(mainBinding.profileBtn);
    }

    public void getProfilePic(ShapeableImageView btn){
        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String token = sharedPref.getString("current_user_token", "none");
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        if(token.length() == 0) return;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersAPI uAPI = retrofit.create(UsersAPI.class);

        Call<ResponseBody> call = uAPI.get_profilePic("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    if (response.body() != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        btn.setImageBitmap(bmp);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.d("XXX", t.toString()); }
        });
    }
    private boolean checkIfPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}