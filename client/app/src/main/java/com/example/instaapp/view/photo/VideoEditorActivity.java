package com.example.instaapp.view.photo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.instaapp.R;
import com.example.instaapp.api.ImageAPI;
import com.example.instaapp.databinding.ActivityVideoEditorBinding;
import com.example.instaapp.model.Location;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.Tag;
import com.example.instaapp.model.TagData;
import com.example.instaapp.view.main.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoEditorActivity extends AppCompatActivity {

    private ActivityVideoEditorBinding mainBinding;

    private ExoPlayer exoPlayer;
    private String location_name = "";
    private Uri videoURI;
    private BottomSheetDialog bottomSheetDialog;
    private List<Integer> TagsAdded = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityVideoEditorBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);


        Bundle data = getIntent().getExtras();
        if(data != null) {
            videoURI = data.getParcelable("URI");
            initializePlayer(videoURI);
        }

        mainBinding.locationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(VideoEditorActivity.this, LocationActivity.class);
            startActivityForResult(intent, 2);
        });

        mainBinding.uploadBtn.setOnClickListener(v -> {
            uploadVideo();
        });

        mainBinding.tagsBtn.setOnClickListener(v -> display_tags());
    }

    public void display_tags(){
        if(bottomSheetDialog == null){
            bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.tags_bottom_sheet);
        }

        SharedPreferences pref = getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = pref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageAPI iAPI = retrofit.create(ImageAPI.class);
        Call<List<Tag>> call = iAPI.get_popular_tags();
        call.enqueue(new Callback<List<Tag>>() {
            @Override
            public void onResponse(Call<List<Tag>> call, Response<List<Tag>> response) {
                if(response.isSuccessful()){
                    LayoutInflater inflater = VideoEditorActivity.this.getLayoutInflater();
                    for(Tag tag: response.body()){
                        Chip chip = (Chip) inflater.inflate(R.layout.chip_popular_tag, null, false);
                        chip.setText(tag.getName());
                        chip.setTag(tag.getID());
                        ChipGroup cg = bottomSheetDialog.findViewById(R.id.popularTags);
                        cg.addView(chip);

                        ChipGroup group = bottomSheetDialog.findViewById(R.id.added_tags);
                        chip.setOnClickListener(v -> {
                            if(!TagsAdded.contains(tag.getID())){
                                addTagToLayout(group, tag);
                                TagsAdded.add(tag.getID());
                            }
                        });
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Tag>> call, Throwable t) { Log.d("XXD", t.toString()); }
        });
        bottomSheetDialog.show();

        EditText input = bottomSheetDialog.findViewById(R.id.input_tag_name);
        ChipGroup cg = bottomSheetDialog.findViewById(R.id.added_tags);
        bottomSheetDialog.findViewById(R.id.add_tag_btn).setOnClickListener(v -> addTag(input, cg));

    }
    public void addTag(EditText input, ChipGroup cg) {
        if(input.getText().toString().length() == 0) return;
        SharedPreferences pref = getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = pref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageAPI iAPI = retrofit.create(ImageAPI.class);
        Tag tag = new Tag(input.getText().toString());
        Call<Tag> call = iAPI.add_tag(tag);
        call.enqueue(new Callback<Tag>() {
            @Override
            public void onResponse(Call<Tag> call, Response<Tag> response) {
                if(response.isSuccessful()){
                    if(!TagsAdded.contains(response.body().getID())){
                        TagsAdded.add(response.body().getID());
                        addTagToLayout(cg, response.body());
                    }
                }else{
                    Log.d("XXD", response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<Tag> call, Throwable t) { Log.d("XXD", t.toString()); }
        });
        input.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    public void addTagToLayout(ChipGroup group, Tag tag){
        LayoutInflater inflater = VideoEditorActivity.this.getLayoutInflater();
        Chip chip = (Chip) inflater.inflate(R.layout.chip_tag_editable, null, false);
        chip.setText(tag.getName());
        chip.setTag(tag.getID());
        group.addView(chip);
        chip.setOnCloseIconClickListener(v -> {
            group.removeView(v);
            TagsAdded.remove(TagsAdded.indexOf(tag.getID()));
        });
    }

    public void uploadVideo(){
        File file = new File(getPath(VideoEditorActivity.this, videoURI ));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
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
                    if(location_name.length() > 0) upload_location(id, iAPI);
                    if(TagsAdded.size() > 0) upload_tags(id, iAPI);
                    Intent intent = new Intent(VideoEditorActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", "X"); }
        });
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
    public void upload_tags(BigInteger id, ImageAPI iAPI){
        int[] array = TagsAdded.stream().mapToInt(i -> i).toArray();
        TagData tag = new TagData(id, array);
        Call<Photo> call = iAPI.set_tags(tag);
        call.enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {}
            @Override
            public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("place_name");
                Log.d("XXX", result);
                location_name = result;
            }
        }
    }

    private void initializePlayer(Uri uri) {
        exoPlayer = new ExoPlayer.Builder(VideoEditorActivity.this).build();
        mainBinding.videoView.setPlayer(exoPlayer) ;

        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
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