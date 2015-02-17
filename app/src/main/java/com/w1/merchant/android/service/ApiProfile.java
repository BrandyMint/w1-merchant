package com.w1.merchant.android.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.Profile;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface ApiProfile {

    @GET("/profile")
    public void getProfile(Callback<Profile> cb);

    @GET("/profile")
    public void getProfile(@Nullable @Query("userId") String userId,
                           @Nullable @Query("types") String types,
                           Callback<Profile> cb);
}
