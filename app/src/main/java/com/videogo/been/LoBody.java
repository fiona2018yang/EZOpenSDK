package com.videogo.been;

import android.os.Parcel;
import android.os.Parcelable;

public class LoBody implements Parcelable {
    String name;
    String address;
    String Preset;

    public LoBody() {
    }

    public LoBody(String name, String address, String preset) {
        this.name = name;
        this.address = address;
        Preset = preset;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPreset() {
        return Preset;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPreset(String preset) {
        Preset = preset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
