package com.w1.merchant.android.rest.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.rest.model.Profile;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface ApiProfile {

    @GET("/profile")
    public Observable<Profile> getProfile();

    @GET("/profile")
    public Observable<Profile> getProfile(@Nullable @Query("userId") String userId,
                           @Nullable @Query("types") String types);
}
