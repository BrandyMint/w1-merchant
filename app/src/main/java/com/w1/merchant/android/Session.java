package com.w1.merchant.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.Captcha;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.utils.NetworkUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

// XXX Thread Unsafe
public final class Session {

    private static final String PREFS_NAME = "session";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_CAPTCHA = "captcha";
    private static final String PREF_CAPTCHA_CODE = "captcha_code";
    private static final String PREF_AUTH_CREATE_SYS_UPTIME = "auth_create_sysuptime";

    private static volatile Session sInstance;

    private Context mAppContext;

    @Nullable
    private AuthModel auth;

    public volatile Captcha captcha;

    public volatile String captchaCode;

    public long authCreateSysUptime;

    private boolean mExpired;

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

    static Session onAppInit(Context appContext) {
        synchronized (Session.class) {
            Session s = getInstance();
            s.mAppContext = appContext;
            s.restore();
            return s;
        }
    }

    void save() {
        if (auth == null || mExpired) {
            mAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
        } else {
            Gson gson = NetworkUtils.getInstance().getGson();
            mAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putString(PREF_TOKEN, gson.toJson(auth))
                    .putString(PREF_CAPTCHA, gson.toJson(captcha))
                    .putString(PREF_CAPTCHA_CODE, captchaCode)
                    .putLong(PREF_AUTH_CREATE_SYS_UPTIME, authCreateSysUptime)
                    .commit();
        }
    }

    void restore() {
        SharedPreferences prefs = mAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = NetworkUtils.getInstance().getGson();
        auth = gson.fromJson(prefs.getString(PREF_TOKEN, null), AuthModel.class);
        captcha = gson.fromJson(prefs.getString(PREF_CAPTCHA, null), Captcha.class);
        captchaCode = prefs.getString(PREF_CAPTCHA_CODE, null);
        authCreateSysUptime = prefs.getLong(PREF_AUTH_CREATE_SYS_UPTIME, 0);
    }

    public void clear() {
        auth = null;
        captcha = null;
        captchaCode = null;
        mExpired = false;
        mAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    public void close() {
        if (auth == null) return;

        Observable<Void> observer = RestClient.getApiSessions().logout();
        observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        Session.this.clear();
                    }
                })
                .subscribe();
    }

    public void setAuth(@Nullable AuthModel auth) {
        authCreateSysUptime = System.nanoTime();
        this.auth = auth;
        save();
    }

    @Nullable
    public String getBearer() {
        return  auth == null ? null : auth.token;
    }

    public boolean hasToken() {
        return auth != null;
    }


    /**
     * @return Были запросы с ошибкой "токен просрочен", либо время токена истекло
     */
    public boolean isExpired() {
        return mExpired || isTokenExpired();
    }

    /**
     * @return Токен, скорее всего, просрочен (проверка только по времени, не особо точная)
     */
    private boolean isTokenExpired() {
        if (auth == null) return false;
        return System.nanoTime() > (authCreateSysUptime + (auth.timeout - 10) * 1e9);
    }

    public String getUserId() {
        return auth == null ? "0" : auth.userId;
    }

    public long getAuthTimeout() {
        return auth == null ? 1 : auth.timeout;
    }

    public void markAsExpired() {
        if (auth != null) mExpired = true;
    }

}
