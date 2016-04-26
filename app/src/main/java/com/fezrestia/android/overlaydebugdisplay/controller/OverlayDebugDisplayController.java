package com.fezrestia.android.overlaydebugdisplay.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.overlaydebugdisplay.R;
import com.fezrestia.android.overlaydebugdisplay.service.OverlayDebugDisplayService;
import com.fezrestia.android.overlaydebugdisplay.view.DebugDisplayView;

public class OverlayDebugDisplayController {
    // Log tag.
    private static final String TAG = "OverlayDebugDisplayController";

    // Master context.
    private  Context mContext;

    // Singleton instance
    private static final OverlayDebugDisplayController INSTANCE
                = new OverlayDebugDisplayController();

    // Overlay view.
    private DebugDisplayView mDebView = null;

    /**
     * Life cycle trigger interface.
     */
    public static class LifeCycleTrigger {
        private static final String TAG = LifeCycleTrigger.class.getSimpleName();
        private static final LifeCycleTrigger INSTANCE = new LifeCycleTrigger();

        // CONSTRUCTOR.
        private LifeCycleTrigger() {
            // NOP.
        }

        /**
         * Get accessor.
         *
         * @return Singleton instance
         */
        public static LifeCycleTrigger getInstance() {
            return INSTANCE;
        }

        /**
         * Start.
         *
         * @param context context
         */
        public void requestStart(Context context) {
            Intent service = new Intent(context, OverlayDebugDisplayService.class);
            ComponentName component = context.startService(service);

            if (Log.IS_DEBUG) {
                if (component != null) {
                    Log.logDebug(TAG, "requestStart() : Component = " + component.toString());
                } else {
                    Log.logDebug(TAG, "requestStart() : Component = NULL");
                }
            }
        }

        /**
         * Stop.
         *
         * @param context context
         */
        public void requestStop(Context context) {
            Intent service = new Intent(context, OverlayDebugDisplayService.class);
            boolean isSuccess = context.stopService(service);

            if (Log.IS_DEBUG) Log.logDebug(TAG, "requestStop() : isSuccess = " + isSuccess);
        }
    }

    /**
     * CONSTRUCTOR.
     */
    private OverlayDebugDisplayController() {
        // NOP.
    }

    /**
     * Get singleton controller instance.
     *
     * @return Singleton instance
     */
    public static synchronized OverlayDebugDisplayController getInstance() {
        return INSTANCE;
    }

    /**
     * Start overlay view finder.
     *
     * @param context context
     */
    public void start(Context context) {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "start() : E");

        if (mDebView != null) {
            // NOP. Already started.
            Log.logError(TAG, "Error. Already started.");
            return;
        }

        // Cache master context.
        mContext = context;

        // Create blinder view.
        mDebView = (DebugDisplayView)
                LayoutInflater.from(mContext).inflate(
                R.layout.overlay_debug_display_view, null);
        mDebView.initialize();
        // Add to window.
        mDebView.addToOverlayWindow();
        // Start.
        mDebView.enable();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "start() : X");
    }

    /**
     * Resume overlay view finder.
     */
    public void resume() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "resume() : E");
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "resume() : X");
    }

    /**
     * Overlay UI is active or not.
     *
     * @return isActive
     */
    public boolean isOverlayActive() {
        return (mDebView != null);
    }

    /**
     * Pause overlay view finder.
     */
    public void pause() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "pause() : E");
        // NOP.
        if (Log.IS_DEBUG) Log.logDebug(TAG, "pause() : X");
    }

    /**
     * Stop overlay view finder.
     */
    public void stop() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "stop() : E");

        if (mDebView == null) {
            // NOP. Already stopped.
            Log.logError(TAG, "Error. Already stopped.");
            return;
        }

        // Release references.
        mContext = null;
        mDebView.disable();
        mDebView.release();
        mDebView.removeFromOverlayWindow();
        mDebView = null;

        if (Log.IS_DEBUG) Log.logDebug(TAG, "stop() : X");
    }
}
