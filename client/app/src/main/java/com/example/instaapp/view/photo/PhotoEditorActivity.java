package com.example.instaapp.view.photo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.example.instaapp.R;
import com.example.instaapp.api.ImageAPI;
import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.ActivityPhotoEditorBinding;
import com.example.instaapp.model.Filter;
import com.example.instaapp.model.Location;
import com.example.instaapp.model.Photo;
import com.example.instaapp.view.main.HomeActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoEditorActivity extends AppCompatActivity {

    private ActivityPhotoEditorBinding mainBinding;
    private File photo_file;
    private Bitmap original_photo;
    private int[] rgb_filter;
    private String filterColorType = "";
    private Boolean flip = false;
    private Boolean flop = false;
    private String location_name = "";

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        Bundle data = getIntent().getExtras();
        if(data != null){
            Uri photoURI = data.getParcelable("URI");
            InputStream stream = null;
            try {
                stream = getContentResolver().openInputStream(photoURI);
                Bitmap b = BitmapFactory.decodeStream(stream);
                if(Objects.equals(data.getString("side"), "back")) original_photo = RotateBitmap(b, 90);
                else if((Objects.equals(data.getString("side"), "front"))) original_photo = RotateBitmap(b, -90);
                mainBinding.displayImg.setImageBitmap(original_photo);

                photo_file = new File(getPath( PhotoEditorActivity.this, photoURI ));
                OutputStream os = new BufferedOutputStream(new FileOutputStream(photo_file));
                original_photo.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.close();
            } catch (FileNotFoundException e) {
                Log.d("XXX", e.toString());
            } catch (IOException e) {
                Log.d("XXX", e.toString());
            }
        }
        mainBinding.filtersBtn.setOnClickListener(v -> {
            if(mainBinding.filtersUi.getVisibility() == View.INVISIBLE) mainBinding.filtersUi.setVisibility(View.VISIBLE);
            else mainBinding.filtersUi.setVisibility(View.INVISIBLE);
        });
        mainBinding.btnCancel.setOnClickListener(v -> {
            mainBinding.filtersUi.setVisibility(View.INVISIBLE);
            mainBinding.radiogrpFilters.clearCheck();
            mainBinding.displayImg.clearColorFilter();
            mainBinding.displayImg.setImageAlpha(255);
        });
        mainBinding.flipBtn.setOnClickListener(v -> {

            if(mainBinding.displayImg.getScaleX() == -1){
                mainBinding.displayImg.setScaleX(1);
                flip = false;
            }
            else {
                mainBinding.displayImg.setScaleX(-1f);
                flip = true;

            }
        });
        mainBinding.flopBtn.setOnClickListener(v -> {
            if(mainBinding.displayImg.getScaleY() == -1) {
                mainBinding.displayImg.setScaleY(1);
                flop = false;
            }
            else {
                mainBinding.displayImg.setScaleY(-1f);
                flop = true;
            }
        });
        mainBinding.radiogrpFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.btn_greyscale:
                        applyGreyScale();
                        break;
                    case R.id.btn_red:
                        rgb_filter = new int[]{255, 0, 0};
                        applyColorFilter();
                        break;
                    case R.id.btn_green:
                        rgb_filter = new int[]{0, 255, 0};
                        applyColorFilter();
                        break;
                    case R.id.btn_blue:
                        rgb_filter = new int[]{0, 0, 255};
                        applyColorFilter();
                        break;
                }

            }
        });
        mainBinding.locationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoEditorActivity.this, LocationActivity.class);
            startActivityForResult(intent, 1);
        });
        mainBinding.uploadBtn.setOnClickListener(v -> upload_photo());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("place_name");
                Log.d("XXX", result);
                location_name = result;
                mainBinding.txtLocation.setText(result);
            }else{
                mainBinding.txtLocation.setText("");
            }
        }
    }

    public void applyGreyScale(){
        filterColorType = "grayscale";
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        mainBinding.displayImg.setColorFilter(cf);
        mainBinding.displayImg.setImageAlpha(128);
    }
    public void applyColorFilter(){
        filterColorType = "tint";
        mainBinding.displayImg.setImageAlpha(255);
        int mul = Color.argb(
                255,
            rgb_filter[0],
            rgb_filter[1],
            rgb_filter[2]
        );
        LightingColorFilter lightingColorFilter = new LightingColorFilter(mul, 0);
        mainBinding.displayImg.setColorFilter(lightingColorFilter);
    }

    public void upload_photo(){
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), photo_file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", photo_file.getName(), requestFile);
        SharedPreferences pref = getSharedPreferences("data", Context.MODE_PRIVATE);
        RequestBody album = RequestBody.create(MediaType.parse("multipart/form-data"),pref.getString("user_id", "lost"));
        String ip = pref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageAPI iAPI = retrofit.create(ImageAPI.class);
        Call<Photo> call = iAPI.upload_photo(body, album);
        call.enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if(response.isSuccessful()){
                    BigInteger id = response.body().getId();
                    upload_filters(id, iAPI);
                    if(location_name.length() > 0) upload_location(id, iAPI);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", "X"); }
        });
    }
    public void upload_filters(BigInteger id, ImageAPI iAPI){
        if(flip){
            Filter filter = new Filter(id, "flip");
            Call<Photo> call = iAPI.set_filter(filter);
            call.enqueue(new Callback<Photo>() {
                @Override
                public void onResponse(Call<Photo> call, Response<Photo> response) {
                    if(response.isSuccessful()){
                        flip = false;
                        upload_filters(id, iAPI);
                    }
                }
                @Override
                public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
            });
        }else if(flop){
            Filter filter = new Filter(id, "flop");
            Call<Photo> call = iAPI.set_filter(filter);
            call.enqueue(new Callback<Photo>() {
                @Override
                public void onResponse(Call<Photo> call, Response<Photo> response) {
                    if(response.isSuccessful()){
                        flop = false;
                        upload_filters(id, iAPI);
                    }
                }
                @Override
                public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
            });
        }else if(filterColorType == "grayscale"){
                Filter filter = new Filter(id, "grayscale");
                Call<Photo> call = iAPI.set_filter(filter);
                call.enqueue(new Callback<Photo>() {
                    @Override
                    public void onResponse(Call<Photo> call, Response<Photo> response) {}
                    @Override
                    public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
                });
        }else if(filterColorType == "tint"){
            Filter filter = new Filter(id, "tint", rgb_filter);
            Call<Photo> call = iAPI.set_filter(filter);
            call.enqueue(new Callback<Photo>() {
                @Override
                public void onResponse(Call<Photo> call, Response<Photo> response) {}
                @Override
                public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
            });
        }
    }

    public void upload_location(BigInteger id, ImageAPI iAPI){
        Location location = new Location(id, location_name);
        Call<Photo> call = iAPI.set_location(location);
        call.enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {}
            @Override
            public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", "X"); }
        });
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
}