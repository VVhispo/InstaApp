package com.example.instaapp.model;
import android.util.Log;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Photo {
    private BigInteger id;
    private String album;
    private String originalName;
    private historyLog[] history;
    private Tag[] tags;
    private String location;

    public Photo(BigInteger id, String album, String originalName, historyLog[] history, Tag[] tags, String location){
        this.id = id;
        this.album = album;
        this.originalName = originalName;
        this.history = history;
        this.tags = tags;
        this.location = location;
    }

    public Tag[] getTags(){
        return this.tags;
    }

    public int getHistoryLength() {
        return history.length;
    }

    public BigInteger getId(){ return id; }

    public String getOriginalName(){
        return originalName;
    }

    public String getLocation(){
        return this.location;
    }

    public String getTime(){
        for(historyLog l: this.history){
            if(Objects.equals(l.getStatus(), "original")){
                long estimatedTime = System.currentTimeMillis() - l.getLastModifiedDate().longValue();
                if(TimeUnit.MILLISECONDS.toMinutes(estimatedTime) < 2){
                    return "just now";
                }else if(TimeUnit.MILLISECONDS.toMinutes(estimatedTime) < 60){
                    return TimeUnit.MILLISECONDS.toMinutes(estimatedTime) + " minutes ago";
                }else if(TimeUnit.MILLISECONDS.toHours(estimatedTime) < 24){
                    return TimeUnit.MILLISECONDS.toHours(estimatedTime) + " hours ago";
                }else{
                    return TimeUnit.MILLISECONDS.toDays(estimatedTime) + " days ago";
                }
            }
        }
        return "";
    }
}
class historyLog{
    private String status;
    private BigInteger lastModifiedDate;

    public historyLog(String status, BigInteger lastModifiedDate){
        this.status = status;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getStatus() {
        return status;
    }

    public BigInteger getLastModifiedDate() {
        return lastModifiedDate;
    }
}