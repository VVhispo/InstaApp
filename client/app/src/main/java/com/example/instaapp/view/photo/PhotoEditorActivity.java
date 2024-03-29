package com.example.instaapp.view.photo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.RadioGroup;

import com.example.instaapp.R;
import com.example.instaapp.api.ImageAPI;
import com.example.instaapp.databinding.ActivityPhotoEditorBinding;
import com.example.instaapp.model.BitmapData;
import com.example.instaapp.model.Filter;
import com.example.instaapp.model.Location;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.Tag;
import com.example.instaapp.model.TagData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private BottomSheetDialog bottomSheetDialog;
    private List<Integer> TagsAdded = new ArrayList<>();

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
            photo_file = new File(getPath( PhotoEditorActivity.this, photoURI ));
        }
        original_photo = BitmapData.getInstance().getBitmap();
        mainBinding.displayImg.setImageBitmap(original_photo);
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
                    LayoutInflater inflater = PhotoEditorActivity.this.getLayoutInflater();
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
        LayoutInflater inflater = PhotoEditorActivity.this.getLayoutInflater();
        Chip chip = (Chip) inflater.inflate(R.layout.chip_tag_editable, null, false);
        chip.setText(tag.getName());
        chip.setTag(tag.getID());
        group.addView(chip);
        chip.setOnCloseIconClickListener(v -> {
            group.removeView(v);
            TagsAdded.remove(TagsAdded.indexOf(tag.getID()));
        });
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
                    if(TagsAdded.size() > 0) upload_tags(id, iAPI);
                    finish();
                }
            }
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

    public void upload_filters(BigInteger id, ImageAPI iAPI){
        List<String> filters = new ArrayList<>();
        if(flip) filters.add("flop");
        if(flop) filters.add("flip");
        if(Objects.equals(filterColorType, "grayscale")) filters.add("grayscale");
        else if(Objects.equals(filterColorType, "tint")) filters.add("tint");
        Filter filter;
        if(Objects.equals(filterColorType, "tint")) filter = new Filter(id, filters.toArray(new String[0]), rgb_filter);
        else filter = new Filter(id, filters.toArray(new String[0]));

        Call<Photo> call = iAPI.set_filter(filter);
        call.enqueue(new Callback<Photo>() {
            @Override
            public void onResponse(Call<Photo> call, Response<Photo> response) {
                if(!response.isSuccessful()){
                    try {
                        Log.d("XXX", response.errorBody().string());
                    } catch (IOException e) {}
                }
            }
            @Override
            public void onFailure(Call<Photo> call, Throwable t) { Log.d("XXX", t.toString()); }
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