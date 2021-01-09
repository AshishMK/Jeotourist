package com.x.jeotourist.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.x.jeotourist.R;
import com.x.jeotourist.application.AppController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to provide application related all utilities
 */
public class Utils {

    final public static int[] colors = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN};
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    final public static int PICK_CAMERA_VID_REQUEST = 1005;
    final public static int PERMISSION_REQUEST = 101;
    final public static int PERMISSION_REQUEST_STORAGE = 102;

    public static String getLocationToString(Location location) {
        return (location != null) ?
                (location.getLatitude() + "," + location.getLongitude()) :
                "Unknown location \n" + Utils.sdf.format(
                        new Date().getTime());
    }

    public static String getLocationToString(Location location, String time) {
        return (location != null) ?
                ("latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() +", time: "+ time) :
                "Unknown location";
    }

    public static boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 1000).show();
            } else {
                Toast.makeText(
                        AppController.getInstance(),
                        "Google Services not available",
                        Toast.LENGTH_LONG
                ).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Return the resource directory of the application
     *
     * @return
     */
    public static File getDirectory() {
        File f = new File(Environment.getExternalStorageDirectory().toString() + "/" + AppController.getInstance().getString(R.string.app_name));
        f.mkdirs();
        return f;
    }

    public static File getCacheDirectory() {
        File f = new File(Environment.getExternalStorageDirectory().toString() + "/" + AppController.getInstance().getString(R.string.app_name) + "/.cache");
        f.mkdirs();
        return f;
    }


    public static String getFileName(String ext) {
        return (sdf.format(new Date().getTime()) + "_java" + ext);
    }


    /**
     * Method to perform Permission model request response operations for the app with
     * fallback functionality {@link Utils#openSettingApp}
     *
     * @param activity
     * @param PERMISSION_REQUEST_CODE
     * @param fragment
     * @return
     */
    public static PermissionStatus checkPermissions(Activity activity, int PERMISSION_REQUEST_CODE, String[] permissions, Fragment fragment, int msgStringId) {
        ArrayList<String> unGrantedPermissions = new ArrayList<>();
        boolean shouldShowRequestPermissionRationale = false;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permissions[i]);
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permissions[i])) {
                    shouldShowRequestPermissionRationale = true;

                }

            }
        }
        if (unGrantedPermissions.size() > 0) {
            if (shouldShowRequestPermissionRationale) {
                Toast.makeText(activity, msgStringId, Toast.LENGTH_LONG).show();
                openSettingApp(activity);
                return PermissionStatus.ERROR;
            }
            if (fragment == null) {
                ActivityCompat.requestPermissions(activity,
                        permissions, PERMISSION_REQUEST_CODE);
                return PermissionStatus.REQUESTED;
            } else {
                fragment.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                return PermissionStatus.REQUESTED;

            }
        } else {
            return PermissionStatus.SUCCESS;
        }

    }

    /**
     * Method to open App's Settings info screen to manually revoke permissions
     * its a fallback for permission model
     *
     * @param ctx
     */
    public static void openSettingApp(Context ctx) {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + AppController.getInstance().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ctx.startActivity(intent);
    }

public enum PermissionStatus {
    SUCCESS,
    ERROR,
    REQUESTED
}


    /**
     * return bitmap from vector drawables
     * ((BitmapDrawable) AppCompatResources.getDrawable(getTarget().getContext(), R.drawable.ic_thin_arrowheads_pointing_down)).getBitmap()
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        DrawableCompat.setTint(drawable, colors[color]);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    /* Create the NotificationChannel, but only on API 26+ because
    the NotificationChannel class is new and not in the support library
   */
    public static void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = AppController.getInstance().getString(R.string.notification_channel);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            String description = AppController.getInstance().getString(R.string.notification_channel_msg);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = AppController.getInstance().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            //Silent notification channel
            String NOTIFICATION_CHANNEL_ID = "2";
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, AppController.getInstance().getString(R.string.silent_notification), NotificationManager.IMPORTANCE_LOW
            );
            //Configure the notification channel, NO SOUND
            notificationChannel.setDescription(AppController.getInstance().getString(R.string.silent_notification_msg));
            notificationChannel.setSound(null, null);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    public static String pickVideoFromCameraFragment(Fragment frg) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File f = new File(getDirectory(), getFileName(".mp4"));
        Uri uri = FileProvider.getUriForFile(
                AppController.getInstance(),
                AppController.getInstance().getPackageName().toString() + ".provider",
                f
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        frg.startActivityForResult(
                intent, PICK_CAMERA_VID_REQUEST
        );
        return f.getAbsolutePath();
    }

    public static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(
                    new ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.letv.android.letvsafe",
                            "com.letv.android.letvsafe.AutobootManageActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.startupapp.StartupAppListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.oppo.safe",
                            "com.oppo.safe.permission.startup.StartupAppListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.oppo.safe",
                            "com.oppo.safe.permission.floatwindow.FloatWindowListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.htc.pitroad",
                            "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                    )
            ),
            new Intent().setComponent(
                    new ComponentName(
                            "com.asus.mobilemanager",
                            "com.asus.mobilemanager.entry.FunctionActivity"
                    )
            ).setData(Uri.parse("mobilemanager://function/entry/AutoStart"))
    };


    public static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list =
                context.getPackageManager().queryIntentActivities(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                );
        return list.size() > 0;
    }

}
