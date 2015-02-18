package com.w1.merchant.android;

import android.os.StrictMode;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.w1.merchant.android.utils.FontManager;
import com.w1.merchant.android.utils.NetworkUtils;

public class Application extends android.app.Application {

    private volatile Tracker mAnalyticsTracker;

    @Override
    public void onCreate() {
        if ("debug".equals(BuildConfig.BUILD_TYPE)) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                            // .penaltyDeath()
                    .build());
        }

        super.onCreate();
        NetworkUtils.getInstance().onAppInit(this);
        FontManager.onAppInit(this);
        getTracker();
    }

    public synchronized Tracker getTracker() {
        if (mAnalyticsTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            if (!"release".equals(BuildConfig.BUILD_TYPE)) {
                analytics.setDryRun(true);
                analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            } else {
                analytics.getLogger().setLogLevel(Logger.LogLevel.ERROR);
            }
            analytics.setLocalDispatchPeriod(1000);
            mAnalyticsTracker = analytics.newTracker(R.xml.app_tracker);
        }
        return mAnalyticsTracker;
    }

}	
