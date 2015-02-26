package com.w1.merchant.android.service;

import com.w1.merchant.android.model.AuthCreateModel;
import com.w1.merchant.android.model.AuthModel;
import com.w1.merchant.android.model.AuthPrincipalRequest;
import com.w1.merchant.android.model.Captcha;
import com.w1.merchant.android.model.OneTimePassword;
import com.w1.merchant.android.model.PrincipalUser;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

public interface ApiSessions {

    @GET("/sessions/current")
    public Observable<AuthModel> getCurrent(Callback<AuthModel> cb);

    @POST("/sessions")
    public Observable<AuthModel> auth(@Body AuthCreateModel req);

    @DELETE("/sessions/current")
    public Observable<Void> logout();

    @POST("/captcha")
    public Observable<Captcha> createCaptchaCode(@Body Captcha.CaptchaRequest request);

    @POST("/password/otp")
    public Observable<Void> sendOneTimePassword(@Body OneTimePassword.Request request);

    @POST("/password")
    public Observable<Void> restorePassword(@Path("sendto") String login);

    @POST("/sessions/principal")
    public Observable<AuthModel> authPrincipal(@Body AuthPrincipalRequest request);

    @GET("/principalusers")
    public Observable<List<PrincipalUser>> getPrincipalUsers();

}
