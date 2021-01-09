package com.x.jeotourist.di.module;


import com.x.jeotourist.scene.playerScene.PlayerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/*
 *  Module to inject specified list of activities
 */
@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract PlayerActivity contributePlayerActivity();
}