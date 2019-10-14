package com.videogo.been;

public class WarningData {
    private String time;
    private String address;
    private String location;
    private String path;

    public WarningData() {
    }

    public WarningData(String time, String address, String location, String path) {
        this.time = time;
        this.address = address;
        this.location = location;
        this.path = path;
    }

    public String getTime() {
        return time;
    }

    public String getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }

    public String getPath() {
        return path;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
