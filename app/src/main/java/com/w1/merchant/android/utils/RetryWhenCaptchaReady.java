package com.w1.merchant.android.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.activity.SessionExpiredDialogActivity;
import com.w1.merchant.android.extra.CaptchaDialogFragment;
import com.w1.merchant.android.extra.CaptchaDialogFragmentV4;
import com.w1.merchant.android.model.Profile;
import com.w1.merchant.android.utils.NetworkUtils;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
* Created by alexey on 24.02.15.
*/
public class RetryWhenCaptchaReady implements
        Func1<Observable<? extends Throwable>, Observable<?>>{

    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String CAPTCHA_DIALOG_TAG = "captcha_dialog";

    @Nullable
    private final Activity mActivity;

    @Nullable
    private final Fragment mFragment;

    @Nullable
    private final android.support.v4.app.Fragment mV4Fragment;

    public RetryWhenCaptchaReady(@NonNull Activity activity) {
        mActivity = activity;
        mFragment = null;
        mV4Fragment = null;
    }

    public RetryWhenCaptchaReady(@NonNull Fragment fragment) {
        mActivity = null;
        mFragment = fragment;
        mV4Fragment = null;
    }

    public RetryWhenCaptchaReady(@NonNull android.support.v4.app.Fragment v4fragment) {
        mActivity = null;
        mFragment = null;
        mV4Fragment = v4fragment;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Throwable, Observable<?>>() {

                    @Override
                    public Observable<?> call(Throwable errorNotification) {
                        NetworkUtils.ResponseErrorException ex;
                        if (!(errorNotification instanceof NetworkUtils.ResponseErrorException)) {
                            return Observable.error(errorNotification);
                        }
                        ex = (NetworkUtils.ResponseErrorException) errorNotification;

                        if (ex.isCaptchaError()) {
                            return startRefreshCaptcha(ex);
                        } else if (ex.isErrorInvalidToken()) {
                            onInvalidToken();
                        }

                        // Max retries hit. Just pass the error along.
                        return Observable.error(errorNotification);
                    }
                });
    }

    protected void onInvalidToken() {
        Activity activity = null;
        if (mActivity != null) {
            activity = mActivity;
        } else if (mFragment != null) {
            activity = mFragment.getActivity();
        } else {
            activity = mV4Fragment.getActivity();
        }

        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, SessionExpiredDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (mActivity != null) {
            activity.startActivity(intent);
        } else if (mFragment != null) {
            mFragment.startActivity(intent);
        } else {
            mV4Fragment.startActivity(intent);
        }

    }

    private Observable<Profile> startRefreshCaptcha(final NetworkUtils.ResponseErrorException error) {
        return Observable.create(new Observable.OnSubscribe<Profile>() {
            @Override
            public void call(final Subscriber<? super Profile> subscriber) {
                Activity activity;
                final Context appContext;
                if (mActivity != null) {
                    activity = mActivity;
                } else if (mFragment != null) {
                    activity = mFragment.getActivity();
                } else {
                    activity = mV4Fragment.getActivity();
                }
                if (activity == null) {
                    subscriber.onError(error);
                    return;
                } else {
                    appContext = activity.getApplicationContext();
                }
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(appContext);

                if (mV4Fragment != null) {
                    FragmentManager fm = activity.getFragmentManager();
                    if (fm.findFragmentByTag(CAPTCHA_DIALOG_TAG) == null) {
                        CaptchaDialogFragment dialog = CaptchaDialogFragment.newInstance(error.isErrorInvalidCaptcha()); // XXX
                        @SuppressLint("CommitTransaction") android.app.FragmentTransaction ft = fm.beginTransaction()
                                .addToBackStack(null);

                        dialog.show(ft, CAPTCHA_DIALOG_TAG);
                    }
                } else {
                    android.support.v4.app.FragmentManager fm = mV4Fragment.getActivity().getSupportFragmentManager();
                    if (fm.findFragmentByTag(CAPTCHA_DIALOG_TAG) == null) {
                        CaptchaDialogFragmentV4 dialog = CaptchaDialogFragmentV4.newInstance(error.isErrorInvalidCaptcha()); // XXX
                        @SuppressLint("CommitTransaction") FragmentTransaction ft = fm.beginTransaction()
                                .addToBackStack(null);

                        dialog.show(ft, CAPTCHA_DIALOG_TAG);
                    }
                }

                final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int newDialogStatus = intent.getIntExtra(CaptchaDialogFragment.ACTION_DIALOG_NEW_STATUS, -1);
                        if (DBG) Log.v(TAG, "Captcha dialog new status: " + newDialogStatus);
                        switch (newDialogStatus) {
                            case CaptchaDialogFragment.STATUS_CANCELLED:
                                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                                subscriber.onError(error);// TODO
                                //onCancelled();
                                break;
                            case CaptchaDialogFragment.STATUS_DONE:
                                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                                subscriber.onNext(null);
                                break;
                            default:
                                break;
                        }
                    }
                };
                broadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(CaptchaDialogFragment.ACTION_DIALOG_NEW_STATUS));
                final Subscription subscription = Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        broadcastManager.unregisterReceiver(mBroadcastReceiver);
                    }
                });
                subscriber.add(subscription);
            }
        });
    }
}
