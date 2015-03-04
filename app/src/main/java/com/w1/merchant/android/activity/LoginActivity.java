package com.w1.merchant.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.DialogNoInet;

public class LoginActivity extends FragmentActivity implements LoginFragment.OnFragmentInteractionListener {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String PREFS_FILE_IS_DIALOG_SHOWN = "IntroShown";
    private static final String PREFS_KEY_IS_INTRO_SHOWN = "is_intro_shown";

    public static final int ACT_MENU = 1;
    public static final String TAG_INTRO_DIALOG = "introDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Session.getInstance().clear();

        if (!isNetworkConnected()) {
            DialogFragment dlgNoInet = new DialogNoInet();
            dlgNoInet.show(getSupportFragmentManager(), "dlgNoInet");
            return;
        }

        if (savedInstanceState == null) {
            if (!isIntroShown()) {
                showIntroDialog();
            } else {
                showLoginDialog(false);
            }
        }
    }

    private boolean isIntroShown() {
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE_IS_DIALOG_SHOWN, MODE_PRIVATE);
        return prefs.getBoolean(PREFS_KEY_IS_INTRO_SHOWN, false);
    }

    private void showIntroDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_INTRO_DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        DialogFragment introFragment = new IntroFragment() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                super.onDismiss(dialog);
                showLoginDialog(true);
            }
        };
        introFragment.show(ft, TAG_INTRO_DIALOG);

        SharedPreferences prefs = getSharedPreferences(PREFS_FILE_IS_DIALOG_SHOWN, MODE_PRIVATE);
        prefs.edit().clear().putBoolean(PREFS_KEY_IS_INTRO_SHOWN, true).apply();
    }

    private void showLoginDialog(boolean afterLogin) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commitAllowingStateLoss();
    }

    //Проверка доступа в Инет
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    @Override
    public void onAuthDone() {
        //запуск основной Activity
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}