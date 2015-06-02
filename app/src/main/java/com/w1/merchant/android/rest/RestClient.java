package com.w1.merchant.android.rest;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.rest.model.ResponseError;
import com.w1.merchant.android.rest.service.ApiBalance;
import com.w1.merchant.android.rest.service.ApiInvoices;
import com.w1.merchant.android.rest.service.ApiPayments;
import com.w1.merchant.android.rest.service.ApiProfile;
import com.w1.merchant.android.rest.service.ApiSessions;
import com.w1.merchant.android.rest.service.ApiSupport;
import com.w1.merchant.android.rest.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;

import java.util.Locale;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

// http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/
public class RestClient {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static volatile RestClient sInstance;

    private RestAdapter mRestAdapter; // http://stackoverflow.com/a/21250503/2971719

    private volatile ApiBalance mApiBalance;

    private volatile ApiInvoices mApiInvoices;

    private volatile ApiPayments mApiPayments;

    private volatile ApiProfile mApiProfile;

    private volatile ApiSessions mApiSessions;

    private volatile ApiSupport mApiSupport;

    private volatile ApiUserEntry mApiUserEntry;

    private RestClient() {
        mRestAdapter = new RestAdapter.Builder()
                .setLogLevel(Constants.RETROFIT_LOG_LEVEL)
                .setEndpoint(BuildConfig.API_SERVER_ADDRESS)
                .setConverter(new W1GsonConverter(NetworkUtils.getGson()))
                .setRequestInterceptor(new AddHeadersRequestInterceptor())
                .setErrorHandler(new ResponseErrorExceptionErrorHandler())
                .setClient(new OkClient(NetworkUtils.getInstance().getOkHttpClient()))
                .build();
    }

    public static RestClient getInstance() {
        if (sInstance == null) {
            synchronized (RestClient.class) {
                if (sInstance == null) {
                    sInstance = new RestClient();
                }
            }
        }
        return sInstance;
    }

    public static ApiBalance getApiBalance() {
        RestClient instance = getInstance();
        if (instance.mApiBalance == null) {
            synchronized (RestClient.class) {
                if (instance.mApiBalance == null) {
                    instance.mApiBalance = instance.mRestAdapter.create(ApiBalance.class);
                }
            }
        }
        return instance.mApiBalance;
    }

    public static ApiInvoices getApiInvoices() {
        RestClient instance = getInstance();
        if (instance.mApiInvoices == null) {
            synchronized (RestClient.class) {
                if (instance.mApiInvoices == null) {
                    instance.mApiInvoices = instance.mRestAdapter.create(ApiInvoices.class);
                }
            }
        }
        return instance.mApiInvoices;
    }

    public static ApiPayments getApiPayments() {
        RestClient instance = getInstance();
        if (instance.mApiPayments == null) {
            synchronized (RestClient.class) {
                if (instance.mApiPayments == null) {
                    instance.mApiPayments = instance.mRestAdapter.create(ApiPayments.class);
                }
            }
        }
        return instance.mApiPayments;
    }

    public static ApiProfile getApiProfile() {
        RestClient instance = getInstance();
        if (instance.mApiProfile == null) {
            synchronized (RestClient.class) {
                if (instance.mApiProfile == null) {
                    instance.mApiProfile = instance.mRestAdapter.create(ApiProfile.class);
                }
            }
        }
        return instance.mApiProfile;
    }

    public static ApiSessions getApiSessions() {
        RestClient instance = getInstance();
        if (instance.mApiSessions == null) {
            synchronized (RestClient.class) {
                if (instance.mApiSessions == null) {
                    instance.mApiSessions = instance.mRestAdapter.create(ApiSessions.class);
                }
            }
        }
        return instance.mApiSessions;
    }

    public static ApiSupport getApiSupport() {
        RestClient instance = getInstance();
        if (instance.mApiSupport == null) {
            synchronized (RestClient.class) {
                if (instance.mApiSupport == null) {
                    instance.mApiSupport = instance.mRestAdapter.create(ApiSupport.class);
                }
            }
        }
        return instance.mApiSupport;
    }

    public static ApiUserEntry getApiUserEntry() {
        RestClient instance = getInstance();
        if (instance.mApiUserEntry == null) {
            synchronized (RestClient.class) {
                if (instance.mApiUserEntry == null) {
                    instance.mApiUserEntry = instance.mRestAdapter.create(ApiUserEntry.class);
                }
            }
        }
        return instance.mApiUserEntry;
    }

    private static final class ResponseErrorExceptionErrorHandler implements ErrorHandler {

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
    }

    private static final class AddHeadersRequestInterceptor implements RequestInterceptor {

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

        @TargetApi(21)
        private static String getLangTag() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return Locale.getDefault().toLanguageTag();
            } else {
                return Locale.getDefault().toString().replace("_","-");
            }
        }
    }



}
