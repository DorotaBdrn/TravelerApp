package com.hfad.travelersample2;

public class Destination {

    private String photoUrl;
    private String description;
    private String key;

    public Destination() {
    }

    public Destination(String photoUrl, String description) {
        this.photoUrl = photoUrl;
        this.description = description;
    }

    public Destination(String photoUrl, String description, String key) {
        this.photoUrl = photoUrl;
        this.description = description;
        this.key = key;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

