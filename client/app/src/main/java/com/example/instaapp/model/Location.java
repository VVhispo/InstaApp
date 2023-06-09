package com.example.instaapp.model;

import java.math.BigInteger;

public class Location {
    BigInteger id;
    String location;

    public Location(BigInteger id, String location){
        this.id = id;
        this.location = location;
    }
}
