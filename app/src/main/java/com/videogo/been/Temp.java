package com.videogo.been;

import android.os.Parcel;
import android.os.Parcelable;

public class Temp implements Parcelable {
    private String temp;//温度
    private String Ph;//ph
    private String Oxygen;//氧气
    private String Nitrogen;//氮气
    private String Permanganate;//高锰酸盐
    private String Phosphorus;//磷
    private String Potential;//频率
    private String Time;//时间

    public Temp() {
    }

    public Temp(String temp, String ph, String oxygen, String nitrogen, String permanganate, String phosphorus, String potential, String time) {
        this.temp = temp;
        Ph = ph;
        Oxygen = oxygen;
        Nitrogen = nitrogen;
        Permanganate = permanganate;
        Phosphorus = phosphorus;
        Potential = potential;
        Time = time;
    }

    protected Temp(Parcel in) {
        temp = in.readString();
        Ph = in.readString();
        Oxygen = in.readString();
        Nitrogen = in.readString();
        Permanganate = in.readString();
        Phosphorus = in.readString();
        Potential = in.readString();
        Time = in.readString();
    }

    public static final Creator<Temp> CREATOR = new Creator<Temp>() {
        @Override
        public Temp createFromParcel(Parcel in) {
            return new Temp(in);
        }

        @Override
        public Temp[] newArray(int size) {
            return new Temp[size];
        }
    };

    public String getTemp() {
        return temp;
    }

    public String getPh() {
        return Ph;
    }

    public String getOxygen() {
        return Oxygen;
    }

    public String getNitrogen() {
        return Nitrogen;
    }

    public String getPermanganate() {
        return Permanganate;
    }

    public String getPhosphorus() {
        return Phosphorus;
    }

    public String getPotential() {
        return Potential;
    }

    public String getTime() {
        return Time;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void setPh(String ph) {
        Ph = ph;
    }

    public void setOxygen(String oxygen) {
        Oxygen = oxygen;
    }

    public void setNitrogen(String nitrogen) {
        Nitrogen = nitrogen;
    }

    public void setPermanganate(String permanganate) {
        Permanganate = permanganate;
    }

    public void setPhosphorus(String phosphorus) {
        Phosphorus = phosphorus;
    }

    public void setPotential(String potential) {
        Potential = potential;
    }

    public void setTime(String time) {
        Time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(temp);
        parcel.writeString(Ph);
        parcel.writeString(Oxygen);
        parcel.writeString(Nitrogen);
        parcel.writeString(Permanganate);
        parcel.writeString(Phosphorus);
        parcel.writeString(Potential);
        parcel.writeString(Time);
    }
}
