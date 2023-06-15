package com.example.instaapp.view.photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentDisplayImageBinding;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.Tag;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

public class displayImageFragment extends Fragment {

    FragmentDisplayImageBinding mainBinding;
    Photo photo;
    String ip;
    Boolean displaying_filtered = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentDisplayImageBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();

        photo = (new Gson()).fromJson(getArguments().getString("photo"), Photo.class);

        SharedPreferences pref = this.getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        ip = pref.getString("ip", "");

        setPhoto();

        if(photo.getHistoryLength() > 1){
            mainBinding.filtersBtn.setVisibility(View.VISIBLE);
            mainBinding.filtersBtn.setOnClickListener(v -> {
                if(!displaying_filtered) setFilteredPhoto();
                else setPhoto();
            });
        }
        display_tags();
        mainBinding.infoTextview.setText(photo.getTime());
        if(photo.getLocation() != null){
            mainBinding.infoTextview.setText(photo.getLocation() + ", " + photo.getTime());
        }
        return view;
    }
    public void display_tags(){
        if(photo.getTags().length == 0){
            mainBinding.tagsScrollview.setVisibility(View.INVISIBLE);
        }
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        for(Tag tag: photo.getTags()){
            Chip chip = (Chip) inflater.inflate(R.layout.chip_popular_tag, null, false);
            chip.setText(tag.getName());
            mainBinding.tagsCg.addView(chip);
        }

    }
    public void setPhoto(){
        displaying_filtered = false;
        Glide.with(mainBinding.mainImageView.getContext())
                .load(ip + "/api/photos/getfile/"+photo.getId())
                .into(mainBinding.mainImageView);
    }
    public void setFilteredPhoto(){
        displaying_filtered = true;
        Glide.with(mainBinding.mainImageView.getContext())
                .load(ip + "/api/photos/getfile_filtered/"+photo.getId())
                .into(mainBinding.mainImageView);
    }
}