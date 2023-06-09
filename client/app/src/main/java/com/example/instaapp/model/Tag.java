package com.example.instaapp.model;

import java.math.BigInteger;

public class Tag {
    private BigInteger id;
    private String name;
    private int popularity;

    public Tag(BigInteger id, String name, int popularity){
        this.id = id;
        this.name = name;
        this.popularity = popularity;
    }
}
