package com.w1.merchant.android.rest.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.rest.model.TransactionHistory;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface ApiUserEntry {

    @GET("/userentry/")
    public Observable<TransactionHistory> getEntries(@Nullable @Query("pageNumber") Integer pageNumber,
                           @Nullable @Query("itemsPerPage") Integer itemsPerPage,
                           @Nullable @Query("fromCreateDate") String fromCreateDate,
                           @Nullable @Query("toCreateDate") String toCreateDate,
                           @Nullable @Query("operationType") String operationType,
                           @Nullable @Query("entryStatesIds") String entryStatesIds,
                           @Nullable @Query("currencyId") String currencyId,
                           @Nullable @Query("searchString") String searchString,
                           @Nullable @Query("direction") String direction);
}
