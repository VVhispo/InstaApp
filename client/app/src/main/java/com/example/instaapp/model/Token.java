package com.example.instaapp.model;

import com.google.gson.annotations.SerializedName;

public class Token {
    private String token;
    private String user_id;

    public Token(String token, String user_id) {
        this.token = token;
        this.user_id = user_id;
    }

    public String getToken(){ return this.token; }

    public String getUid(){ return this.user_id; }
}
