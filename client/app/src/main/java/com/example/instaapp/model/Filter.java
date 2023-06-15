package com.example.instaapp.model;

import java.math.BigInteger;

public class Filter {
    private BigInteger id;
    private String[] filters;
    private Tint tint;

    public Filter(BigInteger id, String[] filters){
        this.id = id;
        this.filters = filters;
    }
    public Filter(BigInteger id, String[] filters, int[] tint){
        this.id = id;
        this.filters = filters;
        this.tint = new Tint(tint[0], tint[1], tint[2]);
    }
}

class Tint{
    private int r;
    private int g;
    private int b;
    public Tint(int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
