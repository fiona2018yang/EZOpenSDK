package com.videogo.been;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmRead implements Parcelable {
    private String type0_read;
    private String type1_read;
    private String type2_read;
    private String type3_read;
    private String type4_read;
    private String type5_read;

    public AlarmRead() {
    }

    public AlarmRead(String type0_read, String type1_read, String type2_read, String type3_read, String type4_read, String type5_read) {
        this.type0_read = type0_read;
        this.type1_read = type1_read;
        this.type2_read = type2_read;
        this.type3_read = type3_read;
        this.type4_read = type4_read;
        this.type5_read = type5_read;
    }

    protected AlarmRead(Parcel in) {
        type0_read = in.readString();
        type1_read = in.readString();
        type2_read = in.readString();
        type3_read = in.readString();
        type4_read = in.readString();
        type5_read = in.readString();
    }

    public static final Creator<AlarmRead> CREATOR = new Creator<AlarmRead>() {
        @Override
        public AlarmRead createFromParcel(Parcel in) {
            return new AlarmRead(in);
        }

        @Override
        public AlarmRead[] newArray(int size) {
            return new AlarmRead[size];
        }
    };

    public String getType0_read() {
        return type0_read;
    }

    public String getType1_read() {
        return type1_read;
    }

    public String getType2_read() {
        return type2_read;
    }

    public String getType3_read() {
        return type3_read;
    }

    public String getType4_read() {
        return type4_read;
    }

    public String getType5_read() {
        return type5_read;
    }

    public void setType0_read(String type0_read) {
        this.type0_read = type0_read;
    }

    public void setType1_read(String type1_read) {
        this.type1_read = type1_read;
    }

    public void setType2_read(String type2_read) {
        this.type2_read = type2_read;
    }

    public void setType3_read(String type3_read) {
        this.type3_read = type3_read;
    }

    public void setType4_read(String type4_read) {
        this.type4_read = type4_read;
    }

    public void setType5_read(String type5_read) {
        this.type5_read = type5_read;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type0_read);
        parcel.writeString(type1_read);
        parcel.writeString(type2_read);
        parcel.writeString(type3_read);
        parcel.writeString(type4_read);
        parcel.writeString(type5_read);
    }
}
