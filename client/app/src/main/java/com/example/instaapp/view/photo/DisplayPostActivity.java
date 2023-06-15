package com.example.instaapp.view.photo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.instaapp.R;
import com.example.instaapp.databinding.ActivityDisplayPostBinding;
import com.example.instaapp.databinding.ActivityUserBinding;
import com.example.instaapp.model.Photo;
import com.google.gson.Gson;

public class DisplayPostActivity extends AppCompatActivity {

    ActivityDisplayPostBinding mainBinding;
    Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityDisplayPostBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        Intent i = getIntent();
        if(i == null){
            onBackPressed();
        }
        photo = (new Gson()).fromJson(i.getStringExtra("photo"), Photo.class);

        Bundle b = new Bundle();
        b.putString("photo", i.getStringExtra("photo"));

        Fragment image = new displayImageFragment();
        image.setArguments(b);
        Fragment video = new displayVideoFragment();
        video.setArguments(b);
        if(photo.getOriginalName().contains("mp4")) replaceFragment(video);
        else replaceFragment(image);

    }
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, fragment)
                .commit();
    }
}