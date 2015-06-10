package com.w1.merchant.android.utils;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.ui.IProgressbarProvider;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.functions.Action0;

public class NetworkUtils {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "NetworkUtils";

    private static volatile NetworkUtils sInstance;

    private static volatile Gson sGson;

    private OkHttpClient mOkHttpClient;

    private com.squareup.picasso.Cache mPicassoMemoryCache;

    public static NetworkUtils getInstance() {
        if (sInstance == null) {
            synchronized (NetworkUtils.class) {
                if (sInstance == null) sInstance = new NetworkUtils();
                return sInstance;
            }
        }
        return sInstance;
    }

    public static Gson getGson() {
        if (sGson == null) {
            synchronized (Gson.class) {
                if (sGson == null) {
                    sGson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                            .registerTypeAdapter(Date.class, new DateTypeAdapter())
                            .create();
                }
            }
        }
        return sGson;
    }

    private NetworkUtils() {
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void onAppInit(Context context) {
        initOkHttpClient(context);
        initPicasso(context);
    }

    private void initOkHttpClient(Context context) {
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setConnectTimeout(Constants.CONNECT_TIMEOUT_S, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(Constants.READ_TIMEOUT_S, TimeUnit.SECONDS);
        File httpCacheDir = context.getCacheDir();
        if (httpCacheDir != null) {
            long cacheSize = NetworkUtils.calculateDiskCacheSize(httpCacheDir);
            if (DBG) Log.v(TAG, "cache size, mb: " + cacheSize / 1024 / 1024);
            // HttpResponseCache.install(httpCacheDir, cacheSize);
            Cache cache = new Cache(httpCacheDir, cacheSize);
            mOkHttpClient.setCache(cache);
        }
    }

    private void initPicasso(Context context) {
        mPicassoMemoryCache = new LruCache(context);
        Picasso picasso = new Picasso.Builder(context.getApplicationContext())
                .downloader(new OkHttpDownloader(mOkHttpClient) {
                                @Override
                                public Response load(Uri uri, int networkPolicy) throws IOException {
                                    if (DBG)
                                        Log.v(TAG, "Load uri: " + uri + " net policy: " + networkPolicy);
                                    return super.load(uri, networkPolicy);
                                }
                            }
                )
                .memoryCache(mPicassoMemoryCache)
                .build();
        Picasso.setSingletonInstance(picasso);
    }

    public static long calculateDiskCacheSize(File dir) {
        long size = Constants.MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, Constants.MAX_DISK_CACHE_SIZE), Constants.MIN_DISK_CACHE_SIZE);
    }

    public static void onTrimMemory() {
        synchronized (NetworkUtils.class) {
            if (sInstance != null) {
                if (sInstance.mPicassoMemoryCache != null) sInstance.mPicassoMemoryCache.clear();
            }
        }
    }

    public static class StopProgressAction implements Action0 {

        private final IProgressbarProvider mListener;

        public Object token;

        public StopProgressAction(IProgressbarProvider listener) {
            mListener = listener;
            token = null;
        }

        @Override
        public void call() {
            mListener.stopProgress(token);
        }
    }

}
