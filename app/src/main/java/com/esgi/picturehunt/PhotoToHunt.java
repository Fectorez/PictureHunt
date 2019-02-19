package com.esgi.picturehunt;

import java.io.Serializable;

public class PhotoToHunt implements Serializable {
    private String userId;
    private String image;
    private double latitude;
    private double longitude;

    public PhotoToHunt(){
    }

    public PhotoToHunt(String userId, String image, double latitude, double longitude) {
        this.userId = userId;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public String getImage() {
        return image;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "PhotoToHunt{" +
                "userId='" + userId + '\'' +
                ", image='" + image + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
