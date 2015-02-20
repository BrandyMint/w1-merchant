package com.w1.merchant.android.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.activity.SessionExpiredDialogActivity;
import com.w1.merchant.android.extra.CaptchaDialogFragment;
import com.w1.merchant.android.utils.NetworkUtils;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Выполнение метода API с обработкой капчи(показом диалога)
 * @param <T>
 */
public abstract class ApiRequestTask<T> {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String CAPTCHA_DIALOG_TAG = "captcha_dialog";

    protected abstract void doRequest(Callback<T> callback);

    @Nullable
    protected abstract Activity getContainerActivity();


    /**
     * @param error Ошибка
     */
    protected abstract void onFailure(NetworkUtils.ResponseErrorException error);

    /**
     * Сервер запросил капчу, а юзер отменил её ввод
     */
    protected abstract void onCancelled();


    protected abstract void onSuccess(T t, Response response);


    public void execute() {
        doRequest(mCallback);
    }

    protected void retryRequest(Callback<T> callback) {
        doRequest(callback);
    }

    protected void onInvalidToken(RetrofitError error) {
        Activity activity = getContainerActivity();
        if (activity == null) return;
        Intent intent = new Intent(activity, SessionExpiredDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    private void startRefreshCaptcha(NetworkUtils.ResponseErrorException error) {
        Activity activity = getContainerActivity();
        if (activity == null) {
            onFailure(error);
            return;
        }
        FragmentManager fm = activity.getFragmentManager();
        if (fm.findFragmentByTag(CAPTCHA_DIALOG_TAG) == null) {
            CaptchaDialogFragment dialog = CaptchaDialogFragment.newInstance(error.isErrorInvalidCaptcha()); // XXX
            @SuppressLint("CommitTransaction") FragmentTransaction ft = fm.beginTransaction()
                    .addToBackStack(null);

            dialog.show(ft, CAPTCHA_DIALOG_TAG);
        }

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity);
        broadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(CaptchaDialogFragment.ACTION_DIALOG_NEW_STATUS));
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newDialogStatus = intent.getIntExtra(CaptchaDialogFragment.ACTION_DIALOG_NEW_STATUS, -1);
            if (DBG) Log.v(TAG, "Captcha dialog new status: " + newDialogStatus);
            switch (newDialogStatus) {
                case CaptchaDialogFragment.STATUS_CANCELLED:
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
                    onCancelled();
                    break;
                case CaptchaDialogFragment.STATUS_DONE:
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
                    retryRequest(mCallback);
                    break;
                default:
                    break;
            }
        }
    };

    private final Callback<T> mCallback = new Callback<T>() {
        @Override
        public void success(T t, Response response) {
            onSuccess(t, response);
        }

        @Override
        public void failure(RetrofitError error) {
            NetworkUtils.ResponseErrorException ex = (NetworkUtils.ResponseErrorException)error.getCause();
            if (ex.isCaptchaError()) {
                startRefreshCaptcha(ex);
            } else if (ex.isErrorInvalidToken()) {
                onInvalidToken(error);
            } else {
                onFailure(ex);
            }
        }
    };
}
