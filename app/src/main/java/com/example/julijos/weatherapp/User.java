package com.example.julijos.weatherapp;

public class User {
    private String mEmail;
    private String mId;
    private String mPassword;

    public User() {
    }

    public User(String mEmail, String mId, String mPassword) {
        this.mEmail = mEmail;
        this.mId = mId;
        this.mPassword = mPassword;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmPassword() {
        return mPassword;
    }

    public void setmPassword(String mPassword) {
        this.mPassword = mPassword;
    }
}
