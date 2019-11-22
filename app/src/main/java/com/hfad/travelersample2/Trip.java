package com.hfad.travelersample2;

public class Trip {
    private String destination;
    private String key;

    public Trip() {
    }

    public Trip(String destination) {
        this.destination = destination;
    }

    public Trip(String destination, String key) {
        this.destination = destination;
        this.key = key;
    }

    public String getDestination() {
        return destination;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
