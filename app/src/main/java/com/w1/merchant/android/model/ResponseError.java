package com.w1.merchant.android.model;

public class ResponseError {

    public static final String ERROR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String ERROR_ARGUMENT_ERROR = "ARGUMENT_ERROR";

    public static final String ERROR_NOT_FOUND = "NOT_FOUND";

    public static final String ERROR_INVALID_TOKEN = "invalid_token";

    public static final String ERROR_INSUFFICIENT_SCOPE = "insufficient_scope";

    public static final String ERROR_CAPTCHA_REQUIRED = "captcha_required";

    public static final String ERROR_INVALID_CAPTCHA = "invalid_captcha";

    public String error;

    public String errorDescription;

    @Override
    public String toString() {
        return "ResponseError{" +
                "error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
