package com.w1.merchant.android.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.utils.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by alexey on 27.02.15.
 */
public class ActivityBase extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static void doOnResume(Activity activity) {
        if (!Session.getInstance().hasToken()) {
            if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "No session token. Restart app");
            Utils.restartApp(activity);
            return;
        }

        if (Session.getInstance().isExpired()) {
            if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "Token expired. Show expired dialog");
            SessionExpiredDialogActivity.show(activity);
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doOnResume(this);
    }

}
