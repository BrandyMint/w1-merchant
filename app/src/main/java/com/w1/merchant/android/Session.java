package com.w1.merchant.android;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.Captcha;

public final class Session {

    private static volatile Session sInstance;

    @Nullable
    public volatile String bearer;

    public volatile Captcha captcha;

    public volatile String captchaCode;

    public static Session getInstance() {
        if (sInstance == null) {
            synchronized (Session.class) {
                if (sInstance == null) sInstance = new Session();
                return sInstance;
            }
        }
        return sInstance;
    }

    private Session() {}

    public void clear() {
        synchronized (Session.class) {
            bearer = null;
            captcha = null;
            captchaCode = null;
        }
    }

}
