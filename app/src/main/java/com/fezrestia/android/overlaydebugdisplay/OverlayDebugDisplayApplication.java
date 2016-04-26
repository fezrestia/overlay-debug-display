package com.fezrestia.android.overlaydebugdisplay;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.fezrestia.android.util.log.Log;

public class OverlayDebugDisplayApplication extends Application {
    // Log tag.
    private static final String TAG = "OverlayDebugDisplayApplication";

    // UI thread handler.
    private static final Handler mUiThreadHandler = new Handler();

    // Shared preference accessor.
    private static SharedPreferences mGlobalSharedPreferences = null;

    // SharedPreferences version key.
    private static final String KEY_SHARED_PREFERENCES_VERSION = "key-shared-preferences-version";
    private static final int VAL_SHARED_PREFERENCES_VERSION = 1;

    @Override
    public void onCreate() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR : E");
        super.onCreate();

        // Create shared preferences accessor.
        mGlobalSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Check version.
        int curVersion = mGlobalSharedPreferences.getInt(KEY_SHARED_PREFERENCES_VERSION, 0);
        if (curVersion != VAL_SHARED_PREFERENCES_VERSION) {
            mGlobalSharedPreferences.edit().clear().apply();
            mGlobalSharedPreferences.edit().putInt(
                    KEY_SHARED_PREFERENCES_VERSION,
                    VAL_SHARED_PREFERENCES_VERSION)
                    .apply();
        }

        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR : X");    }

    @Override
    public void onTerminate() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onTerminate() : E");
        super.onTerminate();

        // Release.
        mGlobalSharedPreferences = null;

        if (Log.IS_DEBUG) Log.logDebug(TAG, "onTerminate() : X");
    }

    /**
     * Get UI thread handler.
     *
     * @return Global UI thread handler
     */
    public static Handler getUiThreadHandler() {
        return mUiThreadHandler;
    }

    /**
     * Get global shared preferences instance.
     *
     * @return Global shared preferences
     */
    public static SharedPreferences getGlobalSharedPreferences() {
        return mGlobalSharedPreferences;
    }



}
