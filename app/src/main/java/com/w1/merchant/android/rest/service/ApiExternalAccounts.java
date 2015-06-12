package com.w1.merchant.android.rest.service;

import com.w1.merchant.android.rest.model.ExternalAccount;
import com.w1.merchant.android.rest.model.ModifyExternalAccountRequest;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by alexey on 13.06.15.
 */
public interface ApiExternalAccounts {

    /**
     * Получение списка платежных средств
     */
    @GET("/externalAccounts")
    Observable<ExternalAccount.ResponseList> getExternalAccounts();

    /**
     * Удаление платежного средства
     * @return Тело ответа пусто.
     */
    @DELETE("/externalAccounts/{externalAccountId}")
    Observable<Void> deleteExternalAccount(@Path("externalAccountId") long externalAccountId);

    @PUT("externalAccounts/{externalAccountId}")
    Observable<Void> modifyExternalAccount(
            @Path("externalAccountId") long externalAccountId,
            @Body ModifyExternalAccountRequest request);

    //POST refill/{externalAccountId}

}
