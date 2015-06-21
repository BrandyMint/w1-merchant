package com.w1.merchant.android.rest.service;


import com.w1.merchant.android.rest.model.NotificationsSubscriptionRequest;

import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface ApiPushNotifications {

    @POST("/push/Subscribe.ashx")
    Observable<Void> subscribe(@Body NotificationsSubscriptionRequest request);

    @POST("/push/Unsubscribe.ashx")
    Observable<Void> ubsubscribe(@Body NotificationsSubscriptionRequest request);

}
