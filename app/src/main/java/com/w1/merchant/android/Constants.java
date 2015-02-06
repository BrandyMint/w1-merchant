package com.w1.merchant.android;

import retrofit.RestAdapter;

public final class Constants {

    public static final String LOG_TAG = "W1Merchant";

    // public static final RestAdapter.LogLevel RETROFIT_LOG_LEVEL = BuildConfig.DEBUG ? RestAdapter.LogLevel.BASIC : RestAdapter.LogLevel.NONE;
    public static final RestAdapter.LogLevel RETROFIT_LOG_LEVEL = BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;
    public static final int CONNECT_TIMEOUT_S = 15;
    public static final int READ_TIMEOUT_S = 60;

    public static final String SUPPORT_EMAIL_UAH = "support@w1.ua";

    public static final String SUPPORT_EMAIL_ZAR = "support_sa@walletone.com";

    public static final String SUPPORT_EMAIL_MAIN = "support@w1.ru";

    private static final String URL_MAIN = BuildConfig.API_SERVER_ADDRESS;


    public static final String URL_WALLETONE = "https://www.walletone.com/wallet/client/";

    public static final String HEADER_CAPTCHA_ID = "X-Wallet-CaptchaId";
    public static final String HEADER_CAPTCHA_CODE = "X-Wallet-CaptchaCode";

    private Constants() {}

}
