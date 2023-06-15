package com.example.instaapp.view.user_profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.instaapp.R;
import com.example.instaapp.model.Photo;
import com.example.instaapp.view.photo.DisplayPostActivity;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private final Context context;
    private final List<Photo> photoList;
    private final String ip;

    public PostAdapter(Context context, List<Photo> photoList, String ip) {
        this.context = context;
        this.photoList = photoList;
        this.ip = ip;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Photo photo = photoList.get(position);
        if(photo == null) return;

        if(photo.getHistoryLength() > 1){
            Log.d("logdev", "filtered");
            holder.icon.setVisibility(View.VISIBLE);
        }else if(photo.getOriginalName().contains("mp4")){
            holder.play.setVisibility(View.VISIBLE);
        }

        holder.txtview.setText(photo.getTime());
        if(photo.getLocation() != null){
            holder.txtview.setText(photo.getLocation() + ", " + photo.getTime());
        }

        Glide.with(holder.imageView.getContext())
                .load(ip + "/api/photos/getfile/"+photo.getId())
                .into(holder.imageView);
        holder.imageView.setOnClickListener(v -> {
            ((UserActivity)context).displayPost(photo);
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView txtview;
        ImageView icon;
        ImageView play;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtview = itemView.findViewById(R.id.post_info);
            imageView = itemView.findViewById(R.id.mainImgView);
            icon = itemView.findViewById(R.id.filteredIcon);
            play = itemView.findViewById(R.id.play_img);
            itemView.findViewById(R.id.postMainLayout).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1200));
        }
    }
}
