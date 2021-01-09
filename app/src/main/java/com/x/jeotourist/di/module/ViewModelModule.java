package com.x.jeotourist.di.module;

import androidx.lifecycle.ViewModelProvider;

import com.x.jeotourist.factory.ViewModelFactory;

import dagger.Binds;
import dagger.Module;

/*
 *  Module to inject specified list of ViewModule
 */
@Module
public abstract class ViewModelModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);





}