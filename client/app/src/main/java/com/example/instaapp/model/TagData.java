package com.example.instaapp.model;

import java.math.BigInteger;
import java.util.ArrayList;

public class TagData {
    private BigInteger photo_id;
    private int[] tags;

    public TagData(BigInteger photo_id, int[] tags){
        this.photo_id = photo_id;
        this.tags = tags;
    }

}
