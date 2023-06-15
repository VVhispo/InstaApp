package com.example.instaapp.api;

import com.example.instaapp.model.Filter;
import com.example.instaapp.model.Location;
import com.example.instaapp.model.Photo;
import com.example.instaapp.model.Tag;
import com.example.instaapp.model.TagData;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ImageAPI {
    @Multipart
    @POST("/api/photos")
    Call<Photo> upload_photo(
            @Part MultipartBody.Part file,
            @Part("album") RequestBody album
    );
    @POST("/api/photos/location")
    Call<Photo> set_location(@Body Location location);

    @PATCH("/api/filters")
    Call<Photo> set_filter(@Body Filter filter);

    @GET("/api/tags/popular")
    Call<List<Tag>> get_popular_tags();

    @POST("/api/tags")
    Call<Tag> add_tag(@Body Tag tag);

    @PATCH("/api/photos/tags/mass")
    Call<Photo> set_tags(@Body TagData data);

    @GET("/api/photos/{uID}")
    Call<List<Photo>> getUserPosts(@Path("uID") String uID);

    @GET("/api/photos/getfile/{iID}")
    Call<ResponseBody> getImageFile(@Path("iID") String iID);
}
