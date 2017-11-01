package com.example.changoo.fishing.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.changoo.fishing.util.Formatter;

public class Fish implements Parcelable{
    private String id;
    private String user_id;
    private String name = "물고기";
    private String imageFile;
    private String species ="어종";
    private Double maxFower;
    private Double avgFower;
    private String date;
    private String time;
    private Integer timeing;
    private Double GPS_lat = 0.0;
    private Double GPS_lot = 0.0;

    public Fish() {

    }

    public Fish(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.name = in.readString();
        this.imageFile = in.readString();
        this.species = in.readString();
        this.maxFower = in.readDouble();
        this.avgFower = in.readDouble();
        this.date = in.readString();
        this.time = in.readString();
        this.timeing = in.readInt();
        this.GPS_lat = in.readDouble();
        this.GPS_lot = in.readDouble();
    }

    public Fish(Fish fish) {
        this.id = fish.getId();
        this.user_id = fish.getUser_id();
        this.imageFile = fish.getImageFile();
        this.species = fish.getSpecies();
        this.maxFower = fish.getMaxFower();
        this.avgFower = fish.getAvgFower();
        this.date = fish.getDate();
        this.time = fish.getTime();
        this.timeing = fish.getTimeing();
        this.GPS_lat = fish.getGPS_lat();
        this.GPS_lot = fish.getGPS_lot();

    }

    @Override
    public String toString() {
        return "Fish{" +
                "id='" + id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", imageFile='" + imageFile + '\'' +
                ", species='" + species + '\'' +
                ", maxFower=" + maxFower +
                ", avgFower=" + avgFower +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", timeing=" + timeing +
                ", GPS_lat=" + GPS_lat +
                ", GPS_lot=" + GPS_lot +
                '}';
    }

    public String toHttpParameter() {
        String str = "";
        str += Formatter.toParameter("id", id);
        str += Formatter.toParameter("user_id", user_id);
        str += Formatter.toParameter("name", name);
        str += Formatter.toParameter("imageFile", imageFile);
        str += Formatter.toParameter("species", species);
        str += Formatter.toParameter("maxFower", maxFower.toString());
        str += Formatter.toParameter("avgFower", avgFower.toString());
        str += Formatter.toParameter("date", date);
        str += Formatter.toParameter("time", time);
        str += Formatter.toParameter("timeing", timeing.toString());
        str += Formatter.toParameter("GPS_lat", GPS_lat.toString());
        str += Formatter.toParameter("GPS_lot", GPS_lot.toString());
        return str;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public double getMaxFower() {
        return maxFower;
    }

    public void setMaxFower(double maxFower) {
        this.maxFower = maxFower;
    }

    public double getAvgFower() {
        return avgFower;
    }

    public void setAvgFower(double avgFower) {
        this.avgFower = avgFower;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getTimeing() {
        return timeing;
    }

    public void setTimeing(Integer timeing) {
        this.timeing = timeing;
    }

    public double getGPS_lat() {
        return GPS_lat;
    }

    public void setGPS_lat(double GPS_lat) {
        this.GPS_lat = GPS_lat;
    }

    public Double getGPS_lot() {
        return GPS_lot;
    }

    public void setGPS_lot(Double GPS_lot) {
        this.GPS_lot = GPS_lot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(imageFile);
        dest.writeString(species);
        dest.writeDouble(maxFower);
        dest.writeDouble(avgFower);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeInt(timeing);
        dest.writeDouble(GPS_lat);
        dest.writeDouble(GPS_lot);
    }

    public static final Parcelable.Creator<Fish> CREATOR = new Parcelable.Creator<Fish>() {
        public Fish createFromParcel(Parcel in) {
            return new Fish(in);
        }

        public Fish[] newArray(int size) {
            return new Fish[size];
        }
    };
}