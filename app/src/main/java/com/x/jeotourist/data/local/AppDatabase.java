package com.x.jeotourist.data.local;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.x.jeotourist.data.local.dao.TourDao;
import com.x.jeotourist.data.local.entity.MarkerEntity;
import com.x.jeotourist.data.local.entity.TourDataEntity;


@Database(entities = {TourDataEntity.class, MarkerEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TourDao contentDao();
}
