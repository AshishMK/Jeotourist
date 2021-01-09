package com.x.jeotourist.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MarkerEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long tourId;

    private String title;

    private int color;

    private double lat;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    private Double lng;

    private String video ;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public MarkerEntity() {
    }

    protected MarkerEntity(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.tourId = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
        this.color = (Integer) in.readValue(Integer.class.getClassLoader());
        this.lat = (Double) in.readValue(Double.class.getClassLoader());
        this.lng = (Double) in.readValue(Double.class.getClassLoader());
        this.video = in.readString();
    }

    public static final Creator<MarkerEntity> CREATOR = new Creator<MarkerEntity>() {
        @Override
        public MarkerEntity createFromParcel(Parcel in) {
            return new MarkerEntity(in);
        }

        @Override
        public MarkerEntity[] newArray(int size) {
            return new MarkerEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.tourId);
        dest.writeString(this.title);
        dest.writeValue(color);
        dest.writeValue(lat);
        dest.writeValue(lng);
        dest.writeString(video);
    }

    public long getTourId() {
        return tourId;
    }

    public void setTourId(long tourId) {
        this.tourId = tourId;
    }
}
