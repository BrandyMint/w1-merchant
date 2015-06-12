package com.w1.merchant.android.ui.withdraw;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.thehayro.internal.Constants;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Profile;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.ProviderList;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.util.Collection;
import java.util.HashMap;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.internal.Assertions;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Данные для списка провайдеров
 */
public class ProviderListFragmentData {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String KEY_PROVIDER_LIST = "com.w1.merchant.android.uiProviderListDataFragment.KEY_PROVIDER_LIST";

    private static final String KEY_CURRENT_PAGE = "com.w1.merchant.android.uiProviderListDataFragment.KEY_CURRENT_PAGE";

    private static final String KEY_KEEP_ON_APPENDING = "com.w1.merchant.android.uiProviderListDataFragment.KEY_KEEP_ON_APPENDING";

    private static final String KEY_USER_LOCATION = "com.w1.merchant.android.uiProviderListDataFragment.KEY_USER_LOCATION";

    public static final int ENTRIES_TO_TRIGGER_APPEND = 1;

    // XXX Работает неправильно, поэтому пока просто большое значение
    public static final int ITEMS_PER_PAGE = 100;

    private boolean mKeepOnAppending;

    private int mCurrentPage;

    private String mUserLocation;

    private HashMap<String, Provider> mProviders;

    private Subscription mProvidersSubscription = Subscriptions.unsubscribed();

    private Subscription mProvidersAppendSubscription = Subscriptions.unsubscribed();

    private final Fragment mTargetFragment;

    private final InteractionListener mListener;

    private Handler mHandler;

    private boolean mActivateCacheQueued;

    public ProviderListFragmentData(Fragment fragment, InteractionListener listener) {
        mTargetFragment = fragment;
        mListener = listener;
    }

    public void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        mActivateCacheQueued = false;
        if (savedInstanceState != null) {
            mProviders = (HashMap<String, Provider>)savedInstanceState.getSerializable(KEY_PROVIDER_LIST);
            if (mProviders == null) mProviders = new HashMap<>();
            mKeepOnAppending = savedInstanceState.getBoolean(KEY_KEEP_ON_APPENDING, true);
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 1);
            mUserLocation = savedInstanceState.getString(KEY_USER_LOCATION);
        } else {
            mProviders = new HashMap<>();
            mKeepOnAppending = true;
            mCurrentPage = 1;
            mUserLocation = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!mProviders.isEmpty()) outState.putSerializable(KEY_PROVIDER_LIST, mProviders);
        if (!mKeepOnAppending) outState.putBoolean(KEY_KEEP_ON_APPENDING, false);
        if (mCurrentPage != 1) outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        if (mUserLocation != null) outState.putString(KEY_USER_LOCATION, mUserLocation);
    }

    public void onDestroy() {
        mProvidersSubscription.unsubscribe();
        mProvidersAppendSubscription.unsubscribe();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void onBindViewHolder(int feedLocation, int maxLocation) {
        if (!mKeepOnAppending) {
            if (DBG) Log.v(TAG, "onBindViewHolder: !mKeepOnAppending");
            return;
        }

        if (isAppendActive()) {
            if (DBG) Log.v(TAG, "onBindViewHolder: isAppendActive()");
            return;
        }

        if (mActivateCacheQueued) {
            if (DBG) Log.v(TAG, "onBindViewHolder: mActivateCacheQueued");
            return;
        }

        if (feedLocation < maxLocation - ENTRIES_TO_TRIGGER_APPEND) {
            if (DBG) Log.v(TAG, "feedLocation < maxLocation. " + feedLocation + " < " + maxLocation);
            return;
        }

        if (DBG) Log.v(TAG, "onBindViewHolder activate cache");

        mHandler.postDelayed(mCallActivateCacheInBackground, 64);
        mActivateCacheQueued = true;
    }

    private Runnable mCallActivateCacheInBackground = new Runnable() {
        @Override
        public void run() {
            activateCacheInBackground();
            mActivateCacheQueued = false;
        }
    };

    public Collection<Provider> getProviderList() {
        return mProviders.values();
    }

    public boolean isLoading() {
        return !mProvidersSubscription.isUnsubscribed();
    }

    public boolean isAppendActive() {
        return !mProvidersAppendSubscription.isUnsubscribed();
    }

    public void reloadProviderList() {
        if (!mProvidersSubscription.isUnsubscribed()) return;
        if (isAppendActive()) mProvidersAppendSubscription.unsubscribe();
        Observable<ProviderList> observable;

        mCurrentPage = 1;
        setKeepOnAppending(true);

        if (mUserLocation == null) {
            Observable<Profile> profileObservarble = RestClient.getApiProfile().getProfile();
            observable = profileObservarble
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(new Func1<Profile, Observable<ProviderList>>() {
                        @Override
                        public Observable<ProviderList> call(Profile profile) {
                            if (DBG) Assertions.assertUiThread();
                            String location = "1";
                            Profile.Attribute locationAttribute = profile.findLocation();
                            if (locationAttribute != null) location = locationAttribute.rawValue;
                            mUserLocation = location;
                            return createProviderListObservable();
                        }
                    });
        } else {
            observable = createProviderListObservable();
        }

        mProvidersSubscription = AppObservable.bindFragment(mTargetFragment, observable)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(mTargetFragment))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        mListener.onLoadingStateChanged();
                    }
                })
                .subscribe(mProviderListObserver);
        mListener.onLoadingStateChanged();
    }

    private Observable<ProviderList> createProviderListObservable() {
        return RestClient.getApiPayments().getProviders(mCurrentPage, ITEMS_PER_PAGE,
                "withdraw", mUserLocation, null, null);
    }

    private void setKeepOnAppending(boolean newValue) {
        if (newValue != mKeepOnAppending) {
            mKeepOnAppending = newValue;
            mListener.onKeepOnAppendingChanged(newValue);
        }
    }

    private final Observer<ProviderList> mProviderListObserver = new Observer<ProviderList>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            mListener.onError(mTargetFragment.getText(R.string.network_error), e);
        }

        @Override
        public void onNext(ProviderList providerList) {
            // XXX В данном варианте onBindViewHolder может быть вызван раньше, чем закончится загрузка (unsubscribe)
            for (Provider provider: providerList.providers) mProviders.put(provider.providerId, provider);
            mListener.onProviderListChanged(providerList);
            mCurrentPage += 1;
            if (providerList.providers.isEmpty()) setKeepOnAppending(false);
        }
    };

    private void activateCacheInBackground() {
        if (DBG) Log.v(TAG, "activateCacheInBackground()");
        if (!mKeepOnAppending) return;
        if (isLoading() || isAppendActive() || mUserLocation == null) return;

        Observable<ProviderList> observable = createProviderListObservable();
        mProvidersAppendSubscription = AppObservable.bindFragment(mTargetFragment, observable)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(mTargetFragment))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        mListener.onLoadingStateChanged();
                    }
                })
                .subscribe(mProviderListObserver);
        mListener.onLoadingStateChanged();
    }

    public interface InteractionListener {
        void onLoadingStateChanged();
        void onKeepOnAppendingChanged(boolean newValue);
        void onError(CharSequence description, Throwable e);
        void onProviderListChanged(ProviderList providerList);
    }

}
