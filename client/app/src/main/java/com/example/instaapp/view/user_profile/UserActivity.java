package com.example.instaapp.view.user_profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.instaapp.R;
import com.example.instaapp.api.UsersAPI;
import com.example.instaapp.databinding.ActivityLoginBinding;
import com.example.instaapp.databinding.ActivityUserBinding;
import com.example.instaapp.model.User;
import com.example.instaapp.view.login_register.LoginActivity;
import com.example.instaapp.view.main.HomeActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
    private User user;

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
    }
    public void saveChanges(){
        if(uploaded_photo != null) {
            Bitmap btm = BitmapFactory.decodeFile(uploaded_photo.getPath());
            mainBinding.profilePic.setImageBitmap(btm);
            post_image();
        }
        post_data();
        getSupportFragmentManager().popBackStack();
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
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("XXX", t.toString());
            }
        });
    }
    public void post_data(){
//
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
        if(b) mainBinding.profilePicEdit.setVisibility(View.VISIBLE);
        else {
            mainBinding.profilePicEdit.setVisibility(View.INVISIBLE);
            mainBinding.profilePic.setImageBitmap(pfp);
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