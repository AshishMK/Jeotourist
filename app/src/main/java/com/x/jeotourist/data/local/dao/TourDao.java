package com.x.jeotourist.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.x.jeotourist.data.local.entity.MarkerEntity;
import com.x.jeotourist.data.local.entity.TourDataEntity;

import java.util.List;

@Dao
public interface TourDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertContents(TourDataEntity entry);

    @Query("SELECT * FROM `TourDataEntity` order by id ")
    List<TourDataEntity> getTours();

    @Query("SELECT * FROM `TourDataEntity` where id = :id")
    TourDataEntity getTour(long id);

    @Query("SELECT * FROM `TourDataEntity`  order by id desc limit 1")
    TourDataEntity getLastTour();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMarker(MarkerEntity marker);

    @Query("SELECT * FROM `MarkerEntity` where tourId = :tourId order by id ")
    List<MarkerEntity> getMarkers(long tourId);

    @Query("SELECT * FROM `MarkerEntity` where tourId = :tourId order by id desc limit 1")
    MarkerEntity getLastMarker(long tourId);

    @Query("SELECT * FROM `MarkerEntity` where id = :id")
    MarkerEntity getMarker(long id);

    @Update
    int updateMarker(MarkerEntity markerEntity);

}