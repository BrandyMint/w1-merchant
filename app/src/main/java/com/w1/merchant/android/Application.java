package com.w1.merchant.android;

import android.os.StrictMode;

import com.w1.merchant.android.utils.NetworkUtils;

import java.util.Locale;

public class Application extends android.app.Application {
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
        Locale.setDefault(Locale.US);
    }
}	
