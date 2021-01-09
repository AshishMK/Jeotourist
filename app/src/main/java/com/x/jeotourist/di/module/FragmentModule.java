package com.x.jeotourist.di.module;


import com.x.jeotourist.scene.mainScene.TourListFragment;
import com.x.jeotourist.scene.mapScene.MapFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


/*
 *  Module to inject specified list of fragments
 */
@Module
public abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract TourListFragment contributeTourListFragment();
    @ContributesAndroidInjector
    abstract MapFragment contributeMapFragment();

}
