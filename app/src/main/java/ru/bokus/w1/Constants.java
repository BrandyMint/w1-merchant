package ru.bokus.w1;

public final class Constants {

    public static final String LOG_TAG = "W1Merchant";

    public static final String URL_WALLETONE = "https://www.walletone.com/wallet/client/";
    private static final String URL_MAIN = "https://api.w1.ru/OpenApi/";

    public static final String LOGIN_BEARER = "54344285-82DA-42EA-B7D0-0C9B978FFD89";

    public static final String URL_USERENTRY = URL_MAIN + "userentry/?pageNumber=%1$s&itemsPerPage=25&currencyId=%2$s";
    public static final String URL_INVOICES = URL_MAIN + "invoices?pageNumber=%1$d&itemsPerPage=25";
    public static final String URL_TEMPLATES = URL_MAIN + "payments/templates/";
    public static final String URL_BALANCE = URL_MAIN + "balance";
    public static final String URL_PROFILE = URL_MAIN + "profile?userId=";
    public static final String URL_USERENTRY_PERIOD = URL_MAIN + "userentry/?pageNumber=%3$s&itemsPerPage=1000&fromCreateDate=%1$s&direction=Inc&currencyId=%2$s";
    public static final String URL_USERENTRY_TOTAL = URL_MAIN + "userentry/?pageNumber=%5$s&itemsPerPage=1000&fromCreateDate=%1$s&toCreateDate=%2$s&direction=%3$s&currencyId=%4$s";
    public static final String URL_PROVIDERS = URL_MAIN + "providers/";
    public static final String URL_OTP = URL_MAIN + "otp";
    public static final String URL_PAYMENTS = URL_MAIN + "payments";
    public static final String URL_PAYMENT_STATE = URL_MAIN + "payments/%s/state";


    private Constants() {}

}
