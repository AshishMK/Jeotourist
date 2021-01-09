package com.x.jeotourist.application;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import androidx.fragment.app.Fragment;

import com.x.jeotourist.di.AppComponent.DaggerAppComponent;
import com.x.jeotourist.utils.Utils;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import timber.log.Timber;


/*
 * Application class
 */
public class AppController extends Application implements HasSupportFragmentInjector, HasActivityInjector, HasServiceInjector {
    private static AppController mInstance;

    /*Inject android activities @see @link{#ActivityModule} for list of injected activities*/
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    /*Inject android fragments @see @link{#FragmentModule} for list of injected fragments*/
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingFragmentAndroidInjector;

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingFragmentAndroidInjector;
    }

    /*Inject android services @see @link{#ServiceModule} for list of injected services*/
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceAndroidInjector;

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return dispatchingServiceAndroidInjector;
    }

    /*returns Application object or application context
     * */
    public static synchronized AppController getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (com.x.jeotourist.BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Utils.createNotificationChannel();

        // initialization of Dagger Dependency injection library
        inject();

    }

    public void inject() {
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }


}

