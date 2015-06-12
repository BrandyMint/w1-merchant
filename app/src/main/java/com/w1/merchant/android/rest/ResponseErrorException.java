package com.w1.merchant.android.rest;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.ResponseError;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by alexey on 03.06.15.
 */
public class ResponseErrorException extends RuntimeException {

    @Nullable
    public final ResponseError error;

    public ResponseErrorException(RetrofitError cause, ResponseError error) {
        super(cause);
        this.error = error;
    }

    @Deprecated
    public String getErrorDescription() {
        return getErrorDescription((Resources) null);
    }

    public String getErrorDescription(@Nullable Resources resources) {
        if (error == null) return "";
        if (isErrorCaptchaRequired() && resources != null) {
            // Иначе по API отдается какой-то невразумительный текст
            return resources.getString(R.string.error_you_must_enter_the_verification_code);
        } else if (isErrorInvalidCaptcha() && resources != null) {
            // А тут ещё больший ахтунг
            return resources.getString(R.string.error_captcha_wrong_code);

        }
        return error.getDesription() == null ? "" : error.getDesription();
    }

    public CharSequence getErrorDescription(CharSequence fallbackText, @Nullable Resources resources) {
        String desc = getErrorDescription((Resources) resources);
        return !TextUtils.isEmpty(desc) ? desc : fallbackText;
    }

    @Deprecated
    public CharSequence getErrorDescription(CharSequence fallbackText) {
        return getErrorDescription(fallbackText, null);
    }

    public RetrofitError getRetrofitError() {
        return (RetrofitError) getCause();
    }

    public int getHttpStatus() {
        RetrofitError err = getRetrofitError();
        Response response = err.getResponse();
        if (response == null) return -1;

        return response.getStatus();
    }

    /**
     * @return HTTP код - ошибка клиента
     */
    public boolean isErrorHttp4xx() {
        return getRetrofitError().getKind() == RetrofitError.Kind.HTTP
                && getHttpStatus() >= 400
                && getHttpStatus() < 500;
    }

    /**
     * @return HTTP код - ошибка на сервере
     */
    public boolean isErrorHttp5xx() {
        return getRetrofitError().getKind() == RetrofitError.Kind.HTTP
                && getHttpStatus() >= 500
                && getHttpStatus() < 600;
    }

    /**
     * @return Сетевая ошибка (HTTP ответ обычно не получен)
     */
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
