package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.Balance;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by alexey on 09.02.15.
 */
public interface ApiBalance {

    @GET("/balance")
    public Observable<List<Balance>> getBalance();

    @GET("/balance/{currencyId}")
    public Observable<Balance> getBalance(@Path("currencyId") String currencyId);

}
