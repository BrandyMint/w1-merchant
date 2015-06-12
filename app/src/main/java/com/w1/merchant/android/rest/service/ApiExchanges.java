package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.ExchangeRate;
import com.w1.merchant.android.rest.model.ExchangeRateStatus;
import com.w1.merchant.android.rest.model.ExchangeRequest;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by alexey on 13.06.15.
 */
public interface ApiExchanges {

    /**
     * Получение курсов обмена валют
     */
    @GET("/exchanges/rates")
    Observable<ExchangeRate.ResponseList> getRates();

    /**
     * Создание операции обмена валют
     */
    @POST("/exchanges")
    Observable<ExchangeRateStatus> exchange(@Body ExchangeRequest request);

    /**
     * Получение деталей операции
     */
    @GET("/exchanges/{operationId}")
    Observable<ExchangeRateStatus> getStatus(@Path("operationId") long operationId);

}
