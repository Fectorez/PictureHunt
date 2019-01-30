package com.esgi.picturehunt;

public class PhotoToHunt {
    String modUser;
    String image;
    double latitude;
    double longitude;

    public PhotoToHunt(){
    }

    public PhotoToHunt(String modUser, String image, double latitude, double longitude) {
        this.modUser = modUser;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getModUser() {
        return modUser;
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
