package com.w1.merchant.android.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.TransactionHistory;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface ApiUserEntry {

    @GET("/userentry/")
    public void getEntries(@Nullable @Query("pageNumber") Integer pageNumber,
                           @Nullable @Query("itemsPerPage") Integer itemsPerPage,
                           @Nullable @Query("fromCreateDate") String fromCreateDate,
                           @Nullable @Query("toCreateDate") String toCreateDate,
                           @Nullable @Query("operationType") String operationType,
                           @Nullable @Query("entryStatesIds") String entryStatesIds,
                           @Nullable @Query("currencyId") String currencyId,
                           @Nullable @Query("searchString") String searchString,
                           @Nullable @Query("direction") String direction,
                           Callback<TransactionHistory> cb
                           );
}
