package com.x.jeotourist.di.module;

import android.app.Application;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.x.jeotourist.R;
import com.x.jeotourist.utils.Utils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

;

/*
 * Module to provide api call functionality
 */
@Module
public class ExoPlayerModule {

    public static final int MIN_BUFFER_DURATION = 2000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 5000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 1500;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 2000;
    /*
     * The method returns the Cache object
     * */
    @Provides
    @Singleton
    SimpleExoPlayer provideSimpleExoPlayer(Application application) {
        /* Instantiate a DefaultLoadControl.Builder. */
        DefaultLoadControl.Builder builder = new
                DefaultLoadControl.Builder();

        /*How many milliseconds of media data to buffer at any time. */
        final int loadControlBufferMs = 60000;////DefaultLoadControl.MAX_BUFFER_MS; /* This is 50000 milliseconds in ExoPlayer 2.9.6 */

        /* Configure the DefaultLoadControl to use the same value for */
        builder.setBufferDurationsMs(
                loadControlBufferMs,
                loadControlBufferMs,
                1500,
                2000)
                //.setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(false);
        return new SimpleExoPlayer.Builder(application.getApplicationContext()).setLoadControl(builder.createDefaultLoadControl()).build();
    }


    /*
     * The method returns the Okhttp object
     * */
    @Provides
    @Singleton
    DataSource.Factory provideDataSourceFactory(Application application) {

        return new DefaultDataSourceFactory(application.getApplicationContext(),
                Util.getUserAgent(application.getApplicationContext(), application.getString(R.string.app_name)));
    }

    /*
     * The method returns the Okhttp object
     * */
    @Provides
    @Singleton
    SimpleCache provideSimpleCache(Application application) {
              int   cacheSize = 700 * 1024 * 1024; //100 mb;
        return new SimpleCache(  Utils.getCacheDirectory(),new LeastRecentlyUsedCacheEvictor(cacheSize),new ExoDatabaseProvider(application));
    }

    @Provides
    @Singleton
    CacheDataSourceFactory provideCacheSourceFactory(SimpleCache simpleCache, DataSource.Factory defaultDataSourceFactory) {

        return new CacheDataSourceFactory(simpleCache, defaultDataSourceFactory);
    }

}
