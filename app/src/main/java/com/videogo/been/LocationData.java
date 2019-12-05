package com.videogo.been;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationData implements Parcelable {
    private Object location;
    private String formatted_address;
    private String business;
    private String addressComponent;
    private String pois;
    private String roads;
    private String poiRegions;
    private String sematic_description;
    private String cityCode;

    public LocationData() {
    }

    public LocationData(Object location, String formatted_address, String business, String addressComponent,
                        String pois, String roads, String poiRegions, String sematic_description, String cityCode) {
        this.location = location;
        this.formatted_address = formatted_address;
        this.business = business;
        this.addressComponent = addressComponent;
        this.pois = pois;
        this.roads = roads;
        this.poiRegions = poiRegions;
        this.sematic_description = sematic_description;
        this.cityCode = cityCode;
    }

    protected LocationData(Parcel in) {
        location = in.readString();
        formatted_address = in.readString();
        business = in.readString();
        addressComponent = in.readString();
        pois = in.readString();
        roads = in.readString();
        poiRegions = in.readString();
        sematic_description = in.readString();
        cityCode = in.readString();
    }

    public static final Creator<LocationData> CREATOR = new Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };

    public Object getLocation() {
        return location;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getBusiness() {
        return business;
    }

    public String getAddressComponent() {
        return addressComponent;
    }

    public String getPois() {
        return pois;
    }

    public String getRoads() {
        return roads;
    }

    public String getPoiRegions() {
        return poiRegions;
    }

    public String getSematic_description() {
        return sematic_description;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setLocation(Object location) {
        this.location = location;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public void setAddressComponent(String addressComponent) {
        this.addressComponent = addressComponent;
    }

    public void setPois(String pois) {
        this.pois = pois;
    }

    public void setRoads(String roads) {
        this.roads = roads;
    }

    public void setPoiRegions(String poiRegions) {
        this.poiRegions = poiRegions;
    }

    public void setSematic_description(String sematic_description) {
        this.sematic_description = sematic_description;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString((String) location);
        parcel.writeString(formatted_address);
        parcel.writeString(business);
        parcel.writeString(addressComponent);
        parcel.writeString(pois);
        parcel.writeString(roads);
        parcel.writeString(poiRegions);
        parcel.writeString(sematic_description);
        parcel.writeString(cityCode);
    }
}
