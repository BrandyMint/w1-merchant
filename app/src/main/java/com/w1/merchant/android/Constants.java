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

    public static final String URL_BALANCE = URL_MAIN + "balance";
    public static final String URL_CLOSE_SESSION = URL_MAIN + "sessions/current";
    public static final String URL_INVOICES = URL_MAIN + "invoices?pageNumber=%1$d&itemsPerPage=25";
    public static final String URL_NEW_INVOICE = URL_MAIN + "invoices";
    public static final String URL_OTP = URL_MAIN + "otp";
    public static final String URL_PAYMENT_STATE = URL_MAIN + "payments/%s/state";
    public static final String URL_PAYMENTS = URL_MAIN + "payments";
    public static final String URL_PROFILE = URL_MAIN + "profile?userId=";
    public static final String URL_PROVIDERS = URL_MAIN + "providers/";
    public static final String URL_TEMPLATES = URL_MAIN + "payments/templates/";
    public static final String URL_USERENTRY_PERIOD = URL_MAIN + "userentry/?pageNumber=%3$s&itemsPerPage=1000&fromCreateDate=%1$s&direction=Inc&currencyId=%2$s";
    public static final String URL_USERENTRY_TOTAL = URL_MAIN + "userentry/?pageNumber=%5$s&itemsPerPage=1000&fromCreateDate=%1$s&toCreateDate=%2$s&direction=%3$s&currencyId=%4$s";
    public static final String URL_USERENTRY = URL_MAIN + "userentry/?pageNumber=%1$s&itemsPerPage=25&currencyId=%2$s";
    public static final String URL_WALLETONE = "https://www.walletone.com/wallet/client/";

    public static final String HEADER_CAPTCHA_ID = "X-Wallet-CaptchaId";
    public static final String HEADER_CAPTCHA_CODE = "X-Wallet-CaptchaCode";

    private Constants() {}

}
