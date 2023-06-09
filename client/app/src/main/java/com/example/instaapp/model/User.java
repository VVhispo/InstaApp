package com.example.instaapp.model;

public class User {
    private String name;
    private String lastName;
    private String email;
    private String bio;

    public User(String name, String lastName, String email, String bio){
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.bio = bio;
    }
    public String getFullName(){
        return name + " " + lastName;
    }
    public String getEmail(){
        return email;
    }
    public String getBio(){
        return bio;
    }
    public String getFirstName(){ return name; }
    public String getLastName(){ return lastName; }
    public String toString(){
        return name + "_" + lastName + "_" + email + "_" + bio;
    }
}
