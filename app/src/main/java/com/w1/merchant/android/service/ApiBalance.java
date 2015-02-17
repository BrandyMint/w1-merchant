package com.w1.merchant.android.service;

import com.w1.merchant.android.model.Balance;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by alexey on 09.02.15.
 */
public interface ApiBalance {

    @GET("/balance")
    public void getBalance(Callback<List<Balance>> cb);

    @GET("/balance/{currencyId}")
    public void getBalance(@Path("currencyId") String currencyId, Callback<Balance> cb);

}
