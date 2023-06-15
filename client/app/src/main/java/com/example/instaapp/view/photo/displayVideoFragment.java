package com.example.instaapp.view.photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaapp.R;
import com.example.instaapp.databinding.FragmentDisplayImageBinding;
import com.example.instaapp.databinding.FragmentDisplayVideoBinding;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.Tag;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

public class displayVideoFragment extends Fragment {

    FragmentDisplayVideoBinding mainBinding;
    Photo photo;
    String ip;
    private ExoPlayer exoPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentDisplayVideoBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();

        photo = (new Gson()).fromJson(getArguments().getString("photo"), Photo.class);

        SharedPreferences pref = this.getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        ip = pref.getString("ip", "");

        display_tags();
        mainBinding.infoTextview.setText(photo.getTime());
        if(photo.getLocation() != null){
            mainBinding.infoTextview.setText(photo.getLocation() + ", " + photo.getTime());
        }
        initializePlayer();
        return view;
    }
    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this.getActivity()).build();
        mainBinding.videoView.setPlayer(exoPlayer) ;

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(ip + "/api/photos/getfile/"+photo.getId()));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
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
}