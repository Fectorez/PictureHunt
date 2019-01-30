package com.esgi.picturehunt;

public class PhotoToHunt {
    String userId;
    String image;
    double latitude;
    double longitude;

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

    public String image() {
        return image;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
