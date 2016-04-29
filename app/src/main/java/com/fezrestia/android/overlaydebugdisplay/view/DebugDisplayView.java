package com.fezrestia.android.overlaydebugdisplay.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fezrestia.android.overlaydebugdisplay.OverlayDebugDisplayApplication;
import com.fezrestia.android.util.log.Log;
import com.fezrestia.android.overlaydebugdisplay.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DebugDisplayView extends FrameLayout {
    // Log tag.
    private static final String TAG = "DebugDisplayView";

    // Root view.
    private FrameLayout mRootView = null;

    // UI.
    private TextView mLogcatTextView = null;

    // Display coordinates.
    private int mDisplayLongLineLength = 0;
    private int mDisplayShortLineLength = 0;

    // Overlay window orientation.
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

    // Window.
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowLayoutParams = null;

    // Interaction flag.
    private static final int INTERACTIVE_FLAGS = 0 // Dummy
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//            | WindowManager.LayoutParams.FLAG_FULLSCREEN
//            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            ;
    private static final int NOT_INTERACTIVE_FLAGS = 0 // Dummy
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//            | WindowManager.LayoutParams.FLAG_FULLSCREEN
            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            ;

    // CONSTRUCTOR.
    public DebugDisplayView(final Context context) {
        this(context, null);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public DebugDisplayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    // CONSTRUCTOR.
    public DebugDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Log.IS_DEBUG) Log.logDebug(TAG, "CONSTRUCTOR");
        // NOP.
    }

    /**
     * Initialize all of configurations.
     */
    public void initialize() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "initialize() : E");

        // Cache instance references.
        cacheInstances();

        // Window related.
        createWindowParameters();

        // Update UI.
        updateTotalUserInterface();

        if (Log.IS_DEBUG) Log.logDebug(TAG, "initialize() : X");
    }

    private void cacheInstances() {
        // Root.
        mRootView = (FrameLayout) findViewById(R.id.root);

        // UI.
        mLogcatTextView = (TextView) findViewById(R.id.logcat);
    }

    private void createWindowParameters() {
        mWindowManager = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);

        mWindowLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                NOT_INTERACTIVE_FLAGS,
                PixelFormat.TRANSLUCENT);
    }

    /**
     * Release all resources.
     */
    public void release() {
        mRootView = null;
        mLogcatTextView = null;

        mWindowManager = null;
        mWindowLayoutParams = null;
    }

    /**
     * Add this view to WindowManager layer.
     */
    public void addToOverlayWindow() {
        // Window parameters.
        updateWindowParams();

        // Add to WindowManager.
        WindowManager winMng = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        winMng.addView(this, mWindowLayoutParams);
    }

    private void updateWindowParams() {
        mWindowLayoutParams.gravity = Gravity.CENTER; //Gravity.LEFT | Gravity.TOP;

        mWindowLayoutParams.x = 0;
        mWindowLayoutParams.y = 0;

        // Full application layout size.
        mWindowLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mWindowLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        switch (mOrientation) {
//            case Configuration.ORIENTATION_LANDSCAPE:
//                mWindowLayoutParams.width = mDisplayLongLineLength;
//                mWindowLayoutParams.height = mDisplayShortLineLength;
//                break;
//
//            case Configuration.ORIENTATION_PORTRAIT:
//                mWindowLayoutParams.width = mDisplayShortLineLength;
//                mWindowLayoutParams.height = mDisplayLongLineLength;
//                break;
//
//            default:
//                throw new RuntimeException("Unexpected orientation.");
//        }

        if (isAttachedToWindow()) {
            mWindowManager.updateViewLayout(this, mWindowLayoutParams);
        }
    }

    /**
     * Remove this view from WindowManager layer.
     */
    public void removeFromOverlayWindow() {
        // Remove from to WindowManager.
        WindowManager winMng = (WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE);
        winMng.removeView(this);
    }

    private void updateTotalUserInterface() {
        // Screen configuration.
        calculateScreenConfiguration();
        // Window layout.
        updateWindowParams();
        // Update layout.
        updateLayoutParams();
    }

    private void calculateScreenConfiguration() {
        // Get display size.
        Display display = mWindowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        final int width = screenSize.x;
        final int height = screenSize.y;
        mDisplayLongLineLength = Math.max(width, height);
        mDisplayShortLineLength = Math.min(width, height);

        // Update state.
        mOrientation = getContext().getResources().getConfiguration().orientation;
    }

    private void updateLayoutParams() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Log.IS_DEBUG) Log.logDebug(TAG,
                "onConfigurationChanged() : [Config=" + newConfig.toString());
        super.onConfigurationChanged(newConfig);

        // Update UI.
        updateTotalUserInterface();
    }

    //// DEBUG INFO COLLECTOR ///

    private boolean mIsAdbThreadActive = false;

    private ExecutorService mAdbThread = null;
    private final AdbThreadFactory mAdbThreadFactory = new AdbThreadFactory();
    private class AdbThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("ADB-Thread");
            return thread;
        }
    }

    private static final int MAX_LOG_LINE_COUNT = 128;

    private final List<String> mLogList = new ArrayList<String>();

    private class AdbLogcatClearTask implements Runnable {
        private final String TAG = "AdbLogcatClearTask";

        @Override
        public void run() {
            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : E");

            try {
                Runtime.getRuntime().exec("logcat -c");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : X");
        }
    }

    private class AdbLogcatLoopTask implements Runnable {
        private final String TAG = "AdbLogcatLoopTask";

        @Override
        public void run() {
            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : E");

            // Command.
            String[] command = { "logcat" };

            Process process = null;
            BufferedReader reader = null;

            try {
                if (Log.IS_DEBUG) Log.logDebug(TAG, "TRY runtime.exec");
                process = Runtime.getRuntime().exec(command);

                if (Log.IS_DEBUG) Log.logDebug(TAG, "TRY process.getInputStream");
                reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()),
                        1024); // Buffer size

                String line;

                if (Log.IS_DEBUG) Log.logDebug(TAG, "TRY reader.readLine");
                while (((line = reader.readLine()) != null) && mIsAdbThreadActive) {

                    synchronized (mLogList) {
                        if (MAX_LOG_LINE_COUNT < mLogList.size()) {
                            mLogList.remove(0);
                        }
                        mLogList.add(line);
                    }

                }

//for (String log : mLogList) {
//    Log.logDebug(TAG, "EACH LOG = " + log);
//}

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : X");
        }
    }

    private static final int RENDERING_INTERVAL_MILLIS = 333;

    private enum LOG_LEVEL {
        ERROR,
        WARNING,
        DEBUG,
        INFO,
        VERBOSE,
    }

    private final RenderTask mRenderTask = new RenderTask();
    private class RenderTask implements Runnable {
        private final String TAG = "RenderTask";

        @Override
        public void run() {
            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : E");

            StringBuilder sb = new StringBuilder();

            int lineCount = mLogcatTextView.getHeight() / mLogcatTextView.getLineHeight();
            synchronized (mLogList) {
                for (int i = mLogList.size() - lineCount; i < mLogList.size(); ++i) {
                    if (i < 0) {
                        // Skip.
                        continue;
                    }

                    // HTML format.
                    sb.append(getHtmlLine(mLogList.get(i)));
                }
            }
            mLogcatTextView.setText(Html.fromHtml(sb.toString()));

            if (mIsAdbThreadActive) {
                OverlayDebugDisplayApplication.getUiThreadHandler().postDelayed(
                        mRenderTask,
                        RENDERING_INTERVAL_MILLIS);
            }

            if (Log.IS_DEBUG) Log.logDebug(TAG, "run() : X");
        }

        private String getHtmlLine(String rawLine) {
            LOG_LEVEL level = LOG_LEVEL.VERBOSE;

            // Each line font.
            if (rawLine == null) {
                // NOP.
            } else if (rawLine.startsWith("E/")) {
                level = LOG_LEVEL.ERROR;
            } else if (rawLine.startsWith("W/")) {
                level = LOG_LEVEL.WARNING;
            } else if (rawLine.startsWith("D/")) {
                level = LOG_LEVEL.DEBUG;
            } else if (rawLine.startsWith("I/")) {
                level = LOG_LEVEL.INFO;
            } else if (rawLine.startsWith("V/")) {
                level = LOG_LEVEL.VERBOSE;
            } else {
                // NOP.
            }

            // HTML format.
            StringBuilder sb = new StringBuilder();
            sb.append("<font color=");
            switch (level) {
                case ERROR:
                    sb.append("#FF0000");
                    break;
                case WARNING:
                    sb.append("#FF0000");
                    break;
                case DEBUG:
                    sb.append("#0000FF");
                    break;
                case INFO:
                    sb.append("#00FF00");
                    break;
                case VERBOSE:
                    sb.append("#FFFFFF");
                    break;
            }
            sb.append(">");
            if (level == LOG_LEVEL.ERROR) {
                sb.append("<b>");
            }
            sb.append(rawLine);
            if (level == LOG_LEVEL.ERROR) {
                sb.append("</b>");
            }
            sb.append("</font>");
            sb.append("<br>");

            return sb.toString();
        }
    }

    public void enable() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "enable() : E");

        synchronized (mLogList) {
            mLogList.clear();
        }

        OverlayDebugDisplayApplication.getUiThreadHandler().removeCallbacks(mRenderTask);
        OverlayDebugDisplayApplication.getUiThreadHandler().post(mRenderTask);

        // Thread.
        if (!mIsAdbThreadActive) {
            mAdbThread = Executors.newSingleThreadExecutor(mAdbThreadFactory);
            mIsAdbThreadActive = true;
        }

        mAdbThread.execute(new AdbLogcatClearTask());
        mAdbThread.execute(new AdbLogcatLoopTask());

        if (Log.IS_DEBUG) Log.logDebug(TAG, "enable() : X");
    }

    public void disable() {
        if (Log.IS_DEBUG) Log.logDebug(TAG, "disable() : E");

        // Thread.
        if (mIsAdbThreadActive) {
            mIsAdbThreadActive = false;
            mAdbThread.shutdown();
            mAdbThread = null;
        }

        OverlayDebugDisplayApplication.getUiThreadHandler().removeCallbacks(mRenderTask);

        if (Log.IS_DEBUG) Log.logDebug(TAG, "disable() : X");
    }
}
