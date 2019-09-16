package com.example.julijos.weatherappAndroid;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.EditText;

public class User {
    private String email;
    private String password;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int validateEmailAndPassword(){

        if (email.isEmpty()) {
            return 1;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return 2;
        }
        if(password.isEmpty()){
            return 3;
        }
        if(password.length() < 6){
            return 4;
        }

        return 0;
    }
}
