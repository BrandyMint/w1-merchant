package com.w1.merchant.android.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.activity.IProgressbarProvider;
import com.w1.merchant.android.model.ResponseError;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import rx.functions.Action0;

public class NetworkUtils {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "NetworkUtils";

    private static volatile NetworkUtils sInstance;

    private final Gson mGson;

    private final GsonConverter mGsonConverter;

    private final W1GsonConverter mRetrofitGsonConverter;

    private OkHttpClient mOkHttpClient;

    private OkClient mRetrofitClient;

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

    private NetworkUtils() {
        mGson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
        mGsonConverter = new GsonConverter(mGson);
        mRetrofitGsonConverter = new W1GsonConverter(mGson);
    }

    public Gson getGson() {
        return mGson;
    }

    public RestAdapter createRestAdapter() {
        RestAdapter.Builder b = new RestAdapter.Builder();

        b.setLogLevel(Constants.RETROFIT_LOG_LEVEL);
        b.setEndpoint(BuildConfig.API_SERVER_ADDRESS)
                .setConverter(mGsonConverter)
                .setRequestInterceptor(sRequestInterceptor)
                .setErrorHandler(mErrorHandler)
                .setConverter(mRetrofitGsonConverter)
                .setClient(mRetrofitClient)
        ;
        return b.build();
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

        mRetrofitClient = new OkClient(mOkHttpClient);
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

    private final RequestInterceptor sRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            Session session = Session.getInstance();
            String bearer = session.getBearer();
            request.addHeader("Authorization", "Bearer " + (bearer != null ? bearer : BuildConfig.API_APP_BEARER));
            request.addHeader("Accept", "application/vnd.wallet.openapi.v1+json");

            synchronized (Session.class) {
                if (!TextUtils.isEmpty(session.captchaCode) && session.captcha != null) {
                    request.addHeader(Constants.HEADER_CAPTCHA_ID, String.valueOf(session.captcha.captchaId));
                    request.addHeader(Constants.HEADER_CAPTCHA_CODE, session.captchaCode);
                }
            }

            String langTag = getLangTag();
            if (!TextUtils.isEmpty(langTag)) {
                request.addHeader("Accept-Language", langTag);
            }
        }
    };

    @TargetApi(21)
    private String getLangTag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.getDefault().toLanguageTag();
        } else {
            return Locale.getDefault().toString().replace("_","-");
        }
    }

    public final ErrorHandler mErrorHandler = new ErrorHandler() {
        @Override
        public Throwable handleError(RetrofitError cause) {
            ResponseError responseError = null;
            try {
                responseError = (ResponseError) cause.getBodyAs(ResponseError.class);
            } catch (Exception ignore) {
                if (DBG) Log.v(TAG, "ignore exception", ignore);
            }

            return new ResponseErrorException(cause, responseError);
        }
    };

    public static class ResponseErrorException extends RuntimeException {

        @Nullable
        public final ResponseError error;

        public ResponseErrorException(RetrofitError cause, ResponseError error) {
            super(cause);
            this.error = error;
        }

        @Deprecated
        public String getErrorDescription() {
            return getErrorDescription((Resources)null);
        }

        public String getErrorDescription(@Nullable Resources resources) {
            if (error == null) return "";
            if (isErrorCaptchaRequired() && resources != null) {
                // Иначе по API отдается какаой-то невразумительный текст
                return resources.getString(R.string.error_you_must_enter_the_verification_code);
            } else if (isErrorInvalidCaptcha() && resources != null) {
                // А тут ещё больший ахтунг
                return resources.getString(R.string.error_captcha_wrong_code);

            }
            return error.getDesription() == null ? "" : error.getDesription();
        }

        public CharSequence getErrorDescription(CharSequence fallbackText, @Nullable Resources resources) {
            String desc = getErrorDescription((Resources)resources);
            return !TextUtils.isEmpty(desc) ? desc : fallbackText;
        }

        @Deprecated
        public CharSequence getErrorDescription(CharSequence fallbackText) {
            return getErrorDescription(fallbackText, null);
        }

        public RetrofitError getRetrofitError() {
            return (RetrofitError)getCause();
        }

        public int getHttpStatus() {
            RetrofitError err = getRetrofitError();
            Response response = err.getResponse();
            if (response == null) return -1;

            return response.getStatus();
        }

        public boolean isNetworkError() {
            return getRetrofitError().getKind() == RetrofitError.Kind.NETWORK;
        }

        public boolean isErrorInvalidToken() {
            return getHttpStatus() == 401
                    || (error != null && ResponseError.ERROR_INVALID_TOKEN.equalsIgnoreCase(error.getTextCode()));
        }

        public boolean isErrorInsufficientScope() {
            return error != null && ResponseError.ERROR_INSUFFICIENT_SCOPE.equalsIgnoreCase(error.getTextCode());
        }

        public boolean isCaptchaError() {
            return isErrorCaptchaRequired() || isErrorInvalidCaptcha();
        }

        public boolean isErrorCaptchaRequired() {
            return error != null && ResponseError.ERROR_CAPTCHA_REQUIRED.equalsIgnoreCase(error.getTextCode());
        }

        public boolean isErrorInvalidCaptcha() {
            return error != null && ResponseError.ERROR_INVALID_CAPTCHA.equalsIgnoreCase(error.getTextCode());
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
