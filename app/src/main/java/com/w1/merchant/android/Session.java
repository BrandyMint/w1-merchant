package com.w1.merchant.android;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.AuthModel;
import com.w1.merchant.android.model.Captcha;

// XXX Thread Unsafe
public final class Session {

    private static volatile Session sInstance;

    @Nullable
    private AuthModel auth;

    public volatile Captcha captcha;

    public volatile String captchaCode;

    public long authCreateSysUptime;

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
        auth = null;
        captcha = null;
        captchaCode = null;
    }

    public void setAuth(@Nullable AuthModel auth) {
        authCreateSysUptime = System.nanoTime();
        this.auth = auth;
    }

    @Nullable
    public String getBearer() {
        return  auth == null ? null : auth.token;
    }

    public boolean hasToken() {
        return auth != null;
    }

    public boolean isTokenExpired() {
        if (auth == null) return false;
        return System.nanoTime() > (authCreateSysUptime + (auth.timeout - 10) * 1e9);
    }

    public String getUserId() {
        return auth == null ? "0" : auth.userId;
    }

    public long getAuthTimeout() {
        return auth == null ? 1 : auth.timeout;
    }

}
