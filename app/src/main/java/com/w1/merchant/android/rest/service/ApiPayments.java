package com.w1.merchant.android.rest.service;


import android.support.annotation.Nullable;

import com.w1.merchant.android.rest.model.CreateScheduleResponse;
import com.w1.merchant.android.rest.model.InitPaymentRequest;
import com.w1.merchant.android.rest.model.InitPaymentStep;
import com.w1.merchant.android.rest.model.InitTemplatePaymentRequest;
import com.w1.merchant.android.rest.model.PaymentDetails;
import com.w1.merchant.android.rest.model.PaymentState;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.ProviderList;
import com.w1.merchant.android.rest.model.Schedule;
import com.w1.merchant.android.rest.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.rest.model.Template;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface ApiPayments {


    @POST("/payments")
    public Observable<InitPaymentStep> initPayment(@Body InitPaymentRequest request);

    @POST("/payments")
    public Observable<InitPaymentStep> initTemplatePayment(@Body InitTemplatePaymentRequest request);

    @PUT("/payments/{paymentId}")
    public Observable<InitPaymentStep> submitPaymentForm(@Path("paymentId") String paymentId, @Body SubmitPaymentFormRequest request);


    @PUT("/payments")
    public Observable<InitPaymentStep> submitPaymentFormExternalId(@Query("externalId") String externalId,
                                            @Body SubmitPaymentFormRequest request);

    @POST("/payments/{paymentId}/code")
    public Observable<Void> sendPaymentOtpCode();

    @GET("/payments/{paymentId}/state")
    public Observable<PaymentState> getPaymentState(@Path("paymentId") String paymentId);

    @GET("/payments/{paymentId}")
    public Observable<PaymentDetails> getPaymentDetails(@Path("paymentId") String paymentId);

    @GET("/payments")
    public Observable<PaymentDetails> getPaymentDetailsExternalId(@Query("externalId") String externalId);

    @POST("/payments/{paymentId}/check")
    public Observable<PaymentDetails> checkPaymentForm(@Path("paymentId") String paymentId);

    @GET("/payments/templates/{templateId}")
    public Observable<Template> getTemplate(@Path("templateId") String templateId);

    @GET("/payments/templates")
    public Observable<Template.TempateList> getTemplates();

    @GET("/payments/templates")
    public Observable<Template.TempateList> getTemplates(@Query("hasSchedule") Boolean hasSchedule);

    @DELETE("/payments/templates/{templateId}")
    public Observable<Void> deleteTemplate(@Path("templateId") String templateId);

    @GET("/providers")
    public Observable<ProviderList> getProviders(@Query("page") int page, @Query("itemsPerPage") int itemsPerPage,
                             @Nullable @Query("providerGroupId") String providerGroupId,
                             @Nullable @Query("locationId") String locationId,
                             @Nullable @Query("searchString") String searchString,
                             @Nullable @Query("isFavourite") Boolean isFavourite);

    @GET("/providers/{providerId}")
    public Observable<Provider> getProvider(@Path("providerId") String providerId);


    @POST("/tasks")
    public Observable<CreateScheduleResponse> createSchedule(@Body Schedule request);

}
