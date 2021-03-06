package com.esgi.picturehunt;

import java.io.Serializable;

public class PhotoAttributes implements Serializable {
    private String mid, description;
    private double score, topicality;

    public PhotoAttributes(){

    }

    public PhotoAttributes(String id, String desc, double score, double topicality){
        this.mid = id;
        this.description = desc;
        this.score = score;
        this.topicality = topicality;
    }

    public String getMid() {
        return mid;
    }

    public String getDescription() {
        return description;
    }

    public double getScore() {
        return score;
    }

    public double getTopicality() {
        return topicality;
    }

    @Override
    public String toString() {
        return "PhotoAttributes{" +
                "mid='" + mid + '\'' +
                ", description='" + description + '\'' +
                ", score=" + score +
                ", topicality=" + topicality +
                '}';
    }
}
