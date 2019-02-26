package com.esgi.picturehunt;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PhotoToHunt implements Serializable {
    private String userId, image, photoId;
    private double latitude;
    private double longitude;
    private List<PhotoAttributes> attributes;

    public PhotoToHunt(){
    }

    public PhotoToHunt(String userId, String image, double latitude, double longitude, String photoId) {
        this.userId = userId;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoId = photoId;
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

    public String getPhotoId() {
        return photoId;
    }

    public List<PhotoAttributes> getAttributes() {
        return attributes;
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
