package com.esgi.picturehunt;

public class PhotoAttributes {
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
                "score=" + score +
                ", topicality=" + topicality +
                '}';
    }
}
