package com.w1.merchant.android.activity;

import android.support.v4.app.FragmentActivity;


public class FragmentActivityBase extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        ActivityBase.doOnResume(this);
    }
}
