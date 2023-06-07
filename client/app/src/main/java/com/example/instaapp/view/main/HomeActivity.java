package com.example.instaapp.view.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.ActivityHomeBinding;
import com.example.instaapp.view.user_profile.UserActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);
        getProfilePic(mainBinding.profileBtn);

        mainBinding.profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserActivity.class);
            startActivity(intent);
        });
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
}