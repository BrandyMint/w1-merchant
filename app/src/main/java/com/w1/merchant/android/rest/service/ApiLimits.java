package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.CurrencyLimit;

import java.util.List;

import retrofit.http.GET;
import rx.Observable;

/**
 * Created by alexey on 13.06.15.
 */
public interface ApiLimits {

    /**
     * Получить лимиты пользователя
     */
    @GET("/limits")
    Observable<List<CurrencyLimit>> getLimits();

}
