package com.example.julijos.weatherapp;

public class Location {
    private String mName;
    private String mId;

    public Location() {
    }

    public Location(String mName, String mId) {
        this.mName = mName;
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }
}
