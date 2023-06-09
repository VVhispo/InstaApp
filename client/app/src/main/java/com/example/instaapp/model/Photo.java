package com.example.instaapp.model;

import java.math.BigInteger;

public class Photo {
    private BigInteger id;
    private String album;
    private String originalName;
    private Object[] history;
    private Tag[] tags;

    public Photo(BigInteger id, String album, String originalName, Object[] history, Tag[] tags){
        this.id = id;
        this.album = album;
        this.originalName = originalName;
        this.history = history;
        this.tags = tags;
    }

    public BigInteger getId(){ return id; }
}
