package com.w1.merchant.android.utils;

import android.content.Context;
import android.net.Uri;
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

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by alexey on 22.01.15.
 */
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
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
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
            String bearer = Session.bearer;
            if (bearer != null) request.addHeader("Authorization", "Bearer " + bearer);
            request.addHeader("Accept", "application/vnd.wallet.openapi.v1+json");


            // TODO: язык
        }
    };


}
