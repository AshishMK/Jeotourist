package com.x.jeotourist.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.x.jeotourist.scene.mainScene.MainActivity;
import com.x.jeotourist.R;
import com.x.jeotourist.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class BackgroundLocationService extends Service {
    private final static int NOTIFICATION_ID = 12345678;
    private final static String PACKAGE_NAME = "com.x.jeotourist.";
    public final static String ACTION_FILE_READ_BROADCAST =
            PACKAGE_NAME + ".action.ACTION_FILE_READ_BROADCAST";
    private final static String EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION = PACKAGE_NAME
            + ".extra.EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION";

    TimerTask refresher;
    Timer timer;

    // FusedLocationProviderClient - Main class for receiving location updates.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private LocationRequest locationRequest;

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private LocationCallback locationCallback;

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private Location currentLocation;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // Initialization code in onCreate or similar:
        timer = new Timer();
        refresher = new TimerTask() {
            @Override
            public void run() {
                new Thread(runnable).start();
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest().setInterval(TimeUnit.SECONDS.toMillis(30))
                .setFastestInterval(TimeUnit.SECONDS.toMillis(15))
                .setMaxWaitTime(TimeUnit.MINUTES.toMillis(2))
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult.getLastLocation() != null) {

                    // Normally, you want to save a new location to a database. We are simplifying
                    // things a bit and just saving it as a local variable, as we only need it again
                    // if a Notification is created (when the user navigates away from app).
                    currentLocation = locationResult.getLastLocation();

                    // Notify our Activity that a new location was added. Again, if this was a
                    // production app, the Activity would be listening for changes to a database
                    // with new locations, but we are simplifying things a bit to focus on just
                    // learning the location side of things.
                    /*      val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                          intent.putExtra(EXTRA_LOCATION, currentLocation)
                          LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)*/
              /*      notificationManager.notify(
                        NOTIFICATION_ID,
                        generateForeGroundNotification(currentLocation)
                    )*/

                }
            }
        };

        subscribeToLocationUpdates();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean cancelLocationTrackingFromNotification = intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION, false);

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates();
            stopSelf();
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unsubscribeToLocationUpdates();
        super.onDestroy();
    }

    private boolean hasPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            writeContent("No Location found at " + Utils.sdf.format(new Date().getTime()));
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void subscribeToLocationUpdates() {
        try {
            if (!hasPermission()) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            startForeground(NOTIFICATION_ID, generateForeGroundNotification(null));
            // first event immediately,  following after 10 seconds each
            timer.scheduleAtFixedRate(refresher, 0, 30000);
        } catch (SecurityException unlikely) {
            stopForeground(true);
            stopSelf();
        }
    }

    private void unsubscribeToLocationUpdates() {
        try {
            Task<Void> removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            removeTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });

        } catch (SecurityException unlikely) {
        }
        refresher.cancel();
        stopForeground(true);
    }

    private Notification generateForeGroundNotification(Location location) {
        String mainNotificationText = Utils.getLocationToString(location);
        String titleText = getString(R.string.app_name);
        NotificationCompat.Style bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(mainNotificationText)
                .setBigContentTitle(titleText);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent cancelIntent = new Intent(this, BackgroundLocationService.class);
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION, true);

        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, "1")
                .setStyle(bigTextStyle)
                .setContentTitle(titleText)
                .setContentText(mainNotificationText)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                        R.drawable.ic_baseline_play_arrow_24,
                        getString(R.string.stop_location_updates_button_text),
                        servicePendingIntent
                )
                .build();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            writeFile();
        }
    };

    private int getFileLineNumber(File file) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        LineNumberReader lineNumberReader = new LineNumberReader(bufferedReader);
        try {
            lineNumberReader.skip(Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timber.v("file writed " + lineNumberReader.getLineNumber());
        return lineNumberReader.getLineNumber() + 1;
    }

    @SuppressLint("MissingPermission")
    private void writeFile() {
        if (!hasPermission()) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    notificationManager.notify(NOTIFICATION_ID, generateForeGroundNotification(location));
                }
                String txt = Utils.getLocationToString(location, Utils.sdf.format(new Date().getTime()));
                Toast.makeText(BackgroundLocationService.this, txt, Toast.LENGTH_LONG).show();
                writeContent(txt);
            }
        });
    }

    private void writeContent(String content) {
        try {
            File myFile = new File(Utils.getDirectory(), "data.txt");
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            int createNewFile = getFileLineNumber(myFile);
            if (createNewFile > 10)
                myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile, createNewFile < 11);

            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            int lineNumber = createNewFile > 10 ? 1 : createNewFile;
            myOutWriter.append("" + lineNumber + ": " + content + "\n");
            myOutWriter.close();
            fOut.close();
            Timber.v("file writed");

        } catch (Exception e) {
            Timber.v("file writed " + e.getLocalizedMessage());
        }
        Intent intent = new Intent(ACTION_FILE_READ_BROADCAST);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }
}
