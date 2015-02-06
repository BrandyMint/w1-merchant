package com.w1.merchant.android.service;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.Toast;

import com.w1.merchant.android.utils.NetworkUtils;

import java.lang.ref.WeakReference;

import retrofit.client.Response;

/**
 * Created by alexey on 15.02.15.
 */
public abstract class ApiRequestTaskActivity<T> extends ApiRequestTask<T> {

    private final int mDefaultResId;
    private final WeakReference<Activity> mActivityRef;

    protected abstract void stopAnimation();

    protected abstract void onSuccess(T t, Response response, Activity activity);

    public ApiRequestTaskActivity(Activity activity, int defaultResId) {
        mActivityRef = new WeakReference<Activity>(activity);
        mDefaultResId = defaultResId;
    }

    @Nullable
    protected Activity getContainerActivity() {
        return mActivityRef.get();
    }

    protected void onFailure(NetworkUtils.ResponseErrorException error) {
        stopAnimation();
        Activity activity = getContainerActivity();
        if (activity != null && !activity.isFinishing()) {
            CharSequence errText = error.getErrorDescription(activity.getText(mDefaultResId));
            Toast toast = Toast.makeText(activity, errText, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
        }
    }

    protected void onCancelled() {
        stopAnimation();
    }

    protected void onSuccess(T t, Response response) {
        stopAnimation();
        Activity activity = getContainerActivity();
        if (activity != null && !activity.isFinishing()) onSuccess(t, response, activity);
    }
}
