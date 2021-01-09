package com.x.jeotourist.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TourDataEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long createdAt;

    private String title;

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

    public TourDataEntity() {
    }

    protected TourDataEntity(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.createdAt = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
    }

    public static final Creator<TourDataEntity> CREATOR = new Creator<TourDataEntity>() {
        @Override
        public TourDataEntity createFromParcel(Parcel in) {
            return new TourDataEntity(in);
        }

        @Override
        public TourDataEntity[] newArray(int size) {
            return new TourDataEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.createdAt);
        dest.writeString(this.title);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
