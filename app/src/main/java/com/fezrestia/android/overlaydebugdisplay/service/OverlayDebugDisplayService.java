package com.fezrestia.android.overlaydebugdisplay.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.overlaydebugdisplay.R;
import com.fezrestia.android.overlaydebugdisplay.activity.OverlayDebugDisplayActivity;
import com.fezrestia.android.overlaydebugdisplay.controller.OverlayDebugDisplayController;

public class OverlayDebugDisplayService extends Service {
    // Log tag.
    private static final String TAG = "OverlayDebugDisplayService";

    // On going notification ID.
    private static final int ONGOING_NOTIFICATION_ID = 100;

    @Override
    public IBinder onBind(Intent intent) {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onBind() : E");
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onBind() : X");
        return null;
    }

    @Override
    public void onCreate() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onCreate() : E");
        super.onCreate();
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onCreate() : X");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onStartCommand() : E");

        // Preference trigger intent.
        Intent preferenceTrigger = new Intent(this, OverlayDebugDisplayActivity.class);
        preferenceTrigger.setFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent notificationContent = PendingIntent.getActivity(
                this,
                0,
                preferenceTrigger,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Foreground notification.
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.application_label))
                .setSmallIcon(R.drawable.overlay_debug_display_ongoing)
                .setContentIntent(notificationContent)
                .build();

        // On foreground.
        startForeground(
                ONGOING_NOTIFICATION_ID,
                notification);

        // Start overlay view finder.
        OverlayDebugDisplayController.getInstance().start(OverlayDebugDisplayService.this);
        OverlayDebugDisplayController.getInstance().resume();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "onStartCommand() : X");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "onDestroy() : E");
        super.onDestroy();

        // Stop overlay view finder.
        OverlayDebugDisplayController.getInstance().pause();
        OverlayDebugDisplayController.getInstance().stop();

        // Stop foreground.
        stopForeground(true);

        if (Log.IS_DEBUG) Log.logDebug(TAG, "onDestroy() : X");
    }
}
