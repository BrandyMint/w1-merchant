package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.AuthCreateModel;
import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.AuthPrincipalRequest;
import com.w1.merchant.android.rest.model.OneTimePassword;
import com.w1.merchant.android.rest.model.PrincipalUser;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

/**
 * Методы из Sessions, которые выполняются с master - токеном
 */
public interface ApiMasterSessions {
    
    @POST("/sessions")
    Observable<AuthModel> auth(@Body AuthCreateModel req);

    // XXX sendOneTimePassword() не работает с master-токенами, сессия должна быть чистой
    @POST("/password/otp")
    Observable<Void> sendOneTimePassword(@Body OneTimePassword.Request request);

    @POST("/sessions/principal")
    Observable<AuthModel> authPrincipal(@Body AuthPrincipalRequest request);

    @DELETE("/sessions/current")
    Observable<Void> logout();

    @GET("/principalusers")
    Observable<List<PrincipalUser>> getPrincipalUsers();

    @Headers("Content-Length: 0")
    @POST("/password")
    Observable<Void> restorePassword(@Path("sendto") String login);

}
