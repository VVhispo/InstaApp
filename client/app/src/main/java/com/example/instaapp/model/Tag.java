package com.example.instaapp.model;

import java.math.BigInteger;

public class Tag {
    private int id;
    private String name;
    private int popularity;

    public Tag(String name){
        this.name = "#" + name;
    }

    public Tag(int id, String name, int popularity){
        this.id = id;
        this.name = name;
        this.popularity = popularity;
    }

    public Tag(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getID(){ return id; }
    public String getName(){ return this.name; }
}
