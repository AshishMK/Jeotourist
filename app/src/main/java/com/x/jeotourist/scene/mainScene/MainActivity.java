package com.x.jeotourist.scene.mainScene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.x.jeotourist.R;
import com.x.jeotourist.data.local.entity.TourDataEntity;
import com.x.jeotourist.scene.mainScene.readFile.ReadFileActivity;
import com.x.jeotourist.scene.mapScene.MapActivity;
import com.x.jeotourist.scene.mapScene.MapFragment;
import com.x.jeotourist.utils.Utils;

public class MainActivity extends AppCompatActivity implements TourListListener {
    Fragment fragment = null;
    boolean hasTwoPanes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasTwoPanes = getResources().getBoolean(R.bool.has_two_panes);
        if (hasTwoPanes) {
            fragment = getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        }
        startPowerSaverIntent(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isGooglePlayServicesAvailable(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.readFile) {
            checkPerMission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTourClicked(TourDataEntity entity) {
        if (fragment != null) {
            MapFragment mapFragment = (MapFragment) fragment;
        mapFragment.setTourIdFromActivity(entity.getId());

    } else {
        startActivity(
                new Intent(this, MapActivity.class).putExtra(
                MapActivity.TOURID,
                entity.getId()).putExtra(MapActivity.TITLE, entity.getTitle()));
    }

}

    private void startPowerSaverIntent(Context context) {
        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (!skipMessage) {
            SharedPreferences.Editor editor = settings.edit();
            boolean foundCorrectIntent = false;
            for (Intent intent : Utils.POWERMANAGER_INTENTS) {
                if (Utils.isCallable(context, intent)) {
                    foundCorrectIntent = true;
                    AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
                    dontShowAgain.setText(R.string.dont_show_again);
                    dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            editor.putBoolean("skipProtectedAppCheck", isChecked);
                            editor.apply();
                        }
                    });
                    new AlertDialog.Builder(context)
                            .setTitle(Build.MANUFACTURER + getString(R.string.protected_apps))
                            .setMessage(
                                    String.format(
                                            context.getString(R.string.autostart_msg),
                                            context.getString(R.string.app_name)
                                    )
                            )
                            .setView(dontShowAgain)
                            .setPositiveButton(R.string.go_to_setting, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        context.startActivity(intent);
                                    } catch (SecurityException e) {

                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    break;
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true);
                editor.apply();
            }
        }
    }

    private void checkPerMission() {
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        Utils.PermissionStatus status = Utils.checkPermissions(
                this,
                Utils.PERMISSION_REQUEST,
                PERMISSIONS,
                null, R.string.storage_permission_msg
        );

        if (status == Utils.PermissionStatus.SUCCESS) {
            startActivity(new Intent(this, ReadFileActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 1) {
            startActivity(new Intent(this, ReadFileActivity.class));
        }
    }
}