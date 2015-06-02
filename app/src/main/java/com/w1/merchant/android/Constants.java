package com.w1.merchant.android;

import retrofit.RestAdapter;

public final class Constants {

    public static final String LOG_TAG = "W1Merchant";

    public static final RestAdapter.LogLevel RETROFIT_LOG_LEVEL = BuildConfig.DEBUG ? RestAdapter.LogLevel.HEADERS_AND_ARGS : RestAdapter.LogLevel.NONE;
    //public static final RestAdapter.LogLevel RETROFIT_LOG_LEVEL = BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;
    public static final int CONNECT_TIMEOUT_S = 15;
    public static final int READ_TIMEOUT_S = 60;

    public static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024;
    public static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024;

    public static final String SUPPORT_EMAIL_UAH = "support@w1.ua";

    public static final String SUPPORT_EMAIL_ZAR = "support_sa@walletone.com";

    public static final String SUPPORT_EMAIL_MAIN = "support@w1.ru";

    public static final String URL_WALLETONE = "https://www.walletone.com/wallet/client/";

    public static final String URL_PROVIDER_LOGO = "https://www.walletone.com/logo/provider/%s.png";

    public static final String HEADER_CAPTCHA_ID = "X-Wallet-CaptchaId";
    public static final String HEADER_CAPTCHA_CODE = "X-Wallet-CaptchaCode";

    private Constants() {}

}
