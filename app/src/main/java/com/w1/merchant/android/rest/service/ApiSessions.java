package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.Captcha;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

public interface ApiSessions {

    @GET("/sessions/current")
    Observable<AuthModel> getCurrent(Callback<AuthModel> cb);

    @DELETE("/sessions/current")
    Observable<Void> logout();

    @POST("/captcha")
    Observable<Captcha> createCaptchaCode(@Body Captcha.CaptchaRequest request);

}
