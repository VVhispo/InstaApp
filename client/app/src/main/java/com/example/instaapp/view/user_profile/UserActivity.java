package com.example.instaapp.view.user_profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.instaapp.R;
import com.example.instaapp.api.ImageAPI;
import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.ActivityLoginBinding;
import com.example.instaapp.databinding.ActivityUserBinding;
import com.example.instaapp.databinding.FragmentEditedBinding;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.User;
import com.example.instaapp.view.login_register.LoginActivity;
import com.example.instaapp.view.main.HomeActivity;
import com.example.instaapp.view.photo.DisplayPostActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity {

    private ActivityUserBinding mainBinding;
    private Bitmap pfp;
    private File uploaded_photo = null;
    private Bitmap uploaded_photo_bm = null;
    private User user;
    private List<Photo> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityUserBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        getProfilePic(mainBinding.profilePic);
        getProfileData();

        Fragment DisplayFragment = new DisplayFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_data_layout, DisplayFragment)
                .commit();

        mainBinding.profilePicEdit.setOnClickListener(v -> loadPicture());
        getPosts();
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    }
    public void displayPost(Photo photo){
        Intent intent = new Intent(UserActivity.this, DisplayPostActivity.class);
        intent.putExtra("photo", (new Gson()).toJson(photo, Photo.class));
        startActivity(intent);
    }
    public void getPosts(){
        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ImageAPI iAPI  = retrofit.create(ImageAPI.class);
        Call<List<Photo>> call = iAPI.getUserPosts(sharedPref.getString("user_id", "lost"));
        call.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if(response.isSuccessful()){
                    postList = response.body();
                    Collections.reverse(postList);
                    PostAdapter adapter = new PostAdapter(UserActivity.this, postList, ip);
                    mainBinding.postRecyclerView.setLayoutManager(new LinearLayoutManager(UserActivity.this));
                    mainBinding.postRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });

    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void setStandardHeight(){
        mainBinding.mainDataLayout.getLayoutParams().height = (int) convertDpToPixel(190, this);
        mainBinding.layoutFrameUser.getLayoutParams().height = (int) convertDpToPixel(300, this);
        mainBinding.mainDataLayout.requestLayout();
        mainBinding.layoutFrameUser.requestLayout();
    }
    public void setEditedHeight(){
        mainBinding.mainDataLayout.getLayoutParams().height = (int) convertDpToPixel(290, this);
        mainBinding.layoutFrameUser.getLayoutParams().height = (int) convertDpToPixel(400, this);
        mainBinding.mainDataLayout.requestLayout();
        mainBinding.layoutFrameUser.requestLayout();
    }

    public void saveChanges(FragmentEditedBinding binding){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Save changes?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                User new_data = new User(
                        binding.inputName.getText().toString(),
                        binding.inputLastName.getText().toString(),
                        binding.inputEmail.getText().toString(),
                        binding.bioInput.getText().toString()
                );
                if(new_data.toString() != user.toString()){
                    post_data(new_data);
                }
                getSupportFragmentManager().popBackStack();
                pfp = uploaded_photo_bm;
                if(uploaded_photo != null) {
                    post_image();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void post_image(){
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), uploaded_photo);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file",uploaded_photo.getName(), requestFile);
        SharedPreferences pref = getSharedPreferences("data", Context.MODE_PRIVATE);
        RequestBody album = RequestBody.create(MediaType.parse("multipart/form-data"),pref.getString("user_id", "lost"));
        String ip = pref.getString("ip", "http://192.168.1.106:3000");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersAPI uAPI = retrofit.create(UsersAPI.class);
        Call<ResponseBody> call = uAPI.post_profilePic(body, album);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                uploaded_photo = null;
                uploaded_photo_bm = null;
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });
    }
    public void post_data(User user_data){
        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersAPI uAPI = retrofit.create(UsersAPI.class);
        String token = sharedPref.getString("current_user_token", "none");

        Call<User> call = uAPI.patch_profileData("Bearer " + token, user_data);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    user = response.body();
                    DisplayFragment fragment = (DisplayFragment) getSupportFragmentManager().findFragmentById(R.id.main_data_layout);
                    fragment.fillTextViews(user);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });

    }
    public void loadPicture(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                try{
                    Uri imgData = data.getData();
                    InputStream stream = getContentResolver().openInputStream(imgData);
                    Bitmap b = BitmapFactory.decodeStream(stream);
                    mainBinding.profilePic.setImageBitmap(b);
                    uploaded_photo_bm = b;
                    uploaded_photo = new File(getPath( this, imgData ));
                }catch(Exception ex){
                    Log.d("XXX", ex.toString());
                }

            }
        }
    }

    public void getProfilePic(ShapeableImageView img){
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
                        img.setImageBitmap(bmp);
                        pfp = bmp;
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) { Log.d("XXX", t.toString()); }
        });
    }

    public void getProfileData(){
        SharedPreferences sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        String token = sharedPref.getString("current_user_token", "none");
        String ip = sharedPref.getString("ip", "http://192.168.1.106:3000");
        if(token.length() == 0) return;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ip)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UsersAPI uAPI = retrofit.create(UsersAPI.class);
        Call<User> call = uAPI.get_profileData("Bearer " + token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    user = response.body();
                    DisplayFragment fragment = (DisplayFragment) getSupportFragmentManager().findFragmentById(R.id.main_data_layout);
                    fragment.fillTextViews(user);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });
    }

    public void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences pref = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear().apply();
                Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void setProfilePicEditable(boolean b){
        if(b) {
            mainBinding.profilePicEdit.setVisibility(View.VISIBLE);
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            bundle.putString("user_data", userJson);
            getSupportFragmentManager().setFragmentResult("user_data", bundle);
        }
        else {
            mainBinding.profilePic.setImageBitmap(pfp);
            mainBinding.profilePicEdit.setVisibility(View.INVISIBLE);
            DisplayFragment fragment = (DisplayFragment) getSupportFragmentManager().findFragmentById(R.id.main_data_layout);
            if(user != null) fragment.fillTextViews(user);
        }
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_data_layout, fragment)
                .addToBackStack("tag")
                .commit();
    }

    public static String getPath( Context context, Uri uri ) {
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