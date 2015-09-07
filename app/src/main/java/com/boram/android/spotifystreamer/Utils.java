package com.boram.android.spotifystreamer;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * Created by P16640 on 2015-09-04.
 */
public class Utils {
    private static final String LOG_TAG = Utils.class.getSimpleName();

    public static boolean isServiceRunning(Context context, Class<?> service) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(service.getName().equals(serviceInfo.service.getClassName())) {
                Log.d(LOG_TAG, "service is running");
                return true;
            }
        }
        Log.d(LOG_TAG, "service is not running");
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static int checkVersion() {
        return Build.VERSION.SDK_INT;
    }
}
