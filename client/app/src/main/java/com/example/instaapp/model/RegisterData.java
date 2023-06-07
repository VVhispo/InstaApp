package com.example.instaapp.model;

public class RegisterData {
    private String name;
    private String lastName;
    private String email;
    private String password;
    
    public RegisterData(String name, String lastName, String email, String password){
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
