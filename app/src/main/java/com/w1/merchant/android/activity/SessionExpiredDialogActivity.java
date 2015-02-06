package com.w1.merchant.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.w1.merchant.android.R;
import com.w1.merchant.android.utils.Utils;

public class SessionExpiredDialogActivity extends Activity {

    private boolean jobIsDone;

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
