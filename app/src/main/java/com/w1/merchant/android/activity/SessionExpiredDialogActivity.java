package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.utils.Utils;

public class SessionExpiredDialogActivity extends Activity {

    private boolean jobIsDone;

    private static boolean sIsShown;

    public static void show(Context context) {
        if (sIsShown) {
            if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "SessionExpiredDialogActivity is shown. Do nothing");
            return;
        } else {
            sIsShown = true;
        }
        Intent intent = new Intent(context, SessionExpiredDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_expired_dialog);
        setTitle(R.string.warning);
        findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRestartApp();
            }
        });

        Session.getInstance().close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doRestartApp();
    }

    void doRestartApp() {
        if (!jobIsDone) {
            jobIsDone = true;
            Utils.restartApp(SessionExpiredDialogActivity.this);
        }
    }


}
