package com.example.instaapp.api;

import com.example.instaapp.model.LoginData;
import com.example.instaapp.model.Token;
import com.example.instaapp.model.RegisterData;
import com.example.instaapp.model.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UsersAPI {
    @POST("/api/users/register")
    Call<Token> register(@Body RegisterData data);

    @POST("/api/users/login")
    Call<Token> login(@Body LoginData data);

    @GET("/api/users/profilePic")
    Call<ResponseBody> get_profilePic(@Header("Authorization") String token);

    @Multipart
    @POST("/api/users/setProfilePic")
    Call<ResponseBody> post_profilePic(
            @Part MultipartBody.Part file,
            @Part("album") RequestBody album
    );

    @GET("api/users/profile")
    Call<User> get_profileData(@Header("Authorization") String token);

    @PATCH("api/users")
    Call<User> patch_profileData(@Header("Authorization") String token, @Body User user);
}
