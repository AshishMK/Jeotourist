package com.x.jeotourist.services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.x.jeotourist.R;
import com.x.jeotourist.application.AppController;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Service started after boot", Toast.LENGTH_LONG).show();
            ContextCompat.startForegroundService(
                    context,
                    new Intent(AppController.getInstance(), BackgroundLocationService.class));
        }
        else{
            Toast.makeText(context, R.string.storage_permission_msg, Toast.LENGTH_LONG).show();
        }
    }
}
