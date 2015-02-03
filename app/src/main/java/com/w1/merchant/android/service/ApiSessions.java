package com.w1.merchant.android.service;

import com.w1.merchant.android.model.AuthCreateModel;
import com.w1.merchant.android.model.AuthModel;
import com.w1.merchant.android.model.Captcha;
import com.w1.merchant.android.model.OneTimePassword;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface ApiSessions {

    @GET("/sessions/current")
    public void getCurrent(Callback<AuthModel> cb);

    @POST("/sessions")
    public void auth(@Body AuthCreateModel req, Callback<AuthModel> cb);

    @DELETE("/sessions")
    public void logout(Callback<Void> cb);

    @POST("/captcha")
    public void createCaptchaCode(@Body Captcha.CaptchaRequest request, Callback<Captcha> cb);

    @POST("/password/otp")
    public void sendOneTimePassword(@Body OneTimePassword.Request request, Callback<Void> response);

    @POST("/password")
    public void restorePassword(@Path("sendto") String login, Callback<Void> response);
}
