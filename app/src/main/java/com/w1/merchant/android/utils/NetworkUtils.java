package com.w1.merchant.android.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.apache.OkApacheClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.model.ResponseError;

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
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

public class NetworkUtils {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "NetworkUtils";

    private static volatile NetworkUtils sInstance;

    private final Gson mGson;

    private final GsonConverter mGsonConverter;

    private final W1GsonConverter mRetrofitGsonConverter;

    private OkHttpClient mOkHttpClient;

    private OkClient mRetrofitClient;

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

    public HttpClient createApacheOkHttpClient() {
        return new OkApacheClient(mOkHttpClient);
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
        mRetrofitClient = new OkClient(mOkHttpClient);
    }

    private void initPicasso(Context context) {
        Picasso picasso = new Picasso.Builder(context.getApplicationContext())
                .downloader(new OkHttpDownloader(mOkHttpClient) {
                                @Override
                                protected HttpURLConnection openConnection(Uri uri) throws IOException {
                                    if (DBG) Log.v(TAG, "Load uri: " + uri);
                                    return super.openConnection(uri);
                                }
                            }
                ).build();
        Picasso.setSingletonInstance(picasso);
    }

    private final RequestInterceptor sRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            Session session = Session.getInstance();
            String bearer = session.getBearer();
            request.addHeader("Authorization", "Bearer " + (bearer != null ? bearer : BuildConfig.API_APP_BEARER));
            request.addHeader("Accept", "application/vnd.wallet.openapi.v1+json");

            synchronized (Session.class) {
                if (!TextUtils.isEmpty(session.captchaCode)) {
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

        public String getErrorCode() {
            return error == null || error.error == null? "" : error.error;
        }

        public String getErrorDescription() {
            return error == null || error.errorDescription == null ? "" : error.errorDescription;
        }

        public CharSequence getErrorDescription(CharSequence fallbackText) {
            String desc = getErrorDescription();
            return !TextUtils.isEmpty(desc) ? desc : fallbackText;
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
            return getHttpStatus() == 401;
        }

        public boolean isCaptchaError() {
            return isErrorCaptchaRequired() || isErrorInvalidCaptcha();
        }

        public boolean isErrorCaptchaRequired() {
            return error != null && ResponseError.ERROR_CAPTCHA_REQUIRED.equalsIgnoreCase(error.error);
        }

        public boolean isErrorInvalidCaptcha() {
            return error != null && ResponseError.ERROR_INVALID_CAPTCHA.equalsIgnoreCase(error.error);
        }
    }


}
