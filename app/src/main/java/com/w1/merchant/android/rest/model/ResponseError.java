package com.w1.merchant.android.rest.model;

public class ResponseError {

    public static final String ERROR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    public static final String ERROR_ARGUMENT_ERROR = "ARGUMENT_ERROR";

    public static final String ERROR_NOT_FOUND = "NOT_FOUND";

    public static final String ERROR_USER_PASSWORD_NOT_MATCH = "USER_PASSWORD_NOT_MATCH";

    public static final String ERROR_INVALID_TOKEN = "invalid_token";

    public static final String ERROR_INSUFFICIENT_SCOPE = "insufficient_scope";

    public static final String ERROR_CAPTCHA_REQUIRED = "captcha_required";

    public static final String ERROR_INVALID_CAPTCHA = "invalid_captcha";

    public static final String ERROR_AMOUNT_RANGE = "AMOUNT_RANGE_ERROR";

    String error;

    String errorDescription;

    /**
     * @return Текстовый код
     * {@linkplain #ERROR_CAPTCHA_REQUIRED}, {@linkplain #ERROR_INVALID_CAPTCHA}, и т.п.
     */
    public String getTextCode() {
        return error;
    }

    /**
     * @return Описание
     */
    public String getDesription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "ResponseError{" +
                "error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
