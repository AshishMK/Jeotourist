package com.x.jeotourist.di.module;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.x.jeotourist.data.local.AppDatabase;
import com.x.jeotourist.data.local.dao.TourDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DbModule {

    /*
     * The method returns the Database object
     * */
    @Provides
    @Singleton
    AppDatabase provideDatabase(@NonNull Application application) {
        return Room.databaseBuilder(application,
                AppDatabase.class, "tour.db")
                .allowMainThreadQueries().build();
    }



    /*
     * We need the ContentDao module.
     * For this, We need the AppDatabase object
     * So we will define the providers for this here in this module.
     * */

    @Provides
    @Singleton
    TourDao provideMovieDao(@NonNull AppDatabase appDatabase) {
        return appDatabase.contentDao();
    }

}
