package com.x.jeotourist.scene.mainScene.readFile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import com.x.jeotourist.R;
import com.x.jeotourist.databinding.ReadFileActivityBinding;
import com.x.jeotourist.services.BackgroundLocationService;
import com.x.jeotourist.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class ReadFileActivity extends AppCompatActivity {
    WeakReference<Activity> weakActivity;

    /**
     * I am using Data binding
     */
    private ReadFileActivityBinding binding;

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    ForegroundOnlyBroadcastReceiver foregroundOnlyBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        weakActivity = new WeakReference(this);
        foregroundOnlyBroadcastReceiver = new ForegroundOnlyBroadcastReceiver();
        initialiseView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*
     * Initialising the View using Data Binding
     * */
    private void initialiseView() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_read_file);
        readFile();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(foregroundOnlyBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(foregroundOnlyBroadcastReceiver, new IntentFilter(BackgroundLocationService.ACTION_FILE_READ_BROADCAST));
    }

    private void readFile() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    File myFile = new File(Utils.getDirectory(), "data.txt");
                    FileInputStream fIn = new FileInputStream(myFile);
                    BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                    String aDataRow = "";
                    String aBuffer = "";
                    while ((aDataRow = myReader.readLine()) != null) {
                        aBuffer += aDataRow + "\n";
                    }
                    if (weakActivity.get() != null) {
                        String finalABuffer = aBuffer;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.setContent(finalABuffer);
                            }
                        });

                        myReader.close();
                    }
                } catch (Exception e) {

                }

                return null;
            }

        }.execute();

    }

    /**
     * Receiver for location broadcasts from [BackgroundLocationService].
     */
    private class ForegroundOnlyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            readFile();
        }
    }
}