package com.w1.merchant.android;

import com.w1.merchant.android.utils.NetworkUtils;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NetworkUtils.getInstance().onAppInit(this);
    }
}	
