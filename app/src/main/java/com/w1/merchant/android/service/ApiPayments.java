package com.w1.merchant.android.service;


import android.support.annotation.Nullable;

import com.w1.merchant.android.model.CreateScheduleResponse;
import com.w1.merchant.android.model.InitPaymentRequest;
import com.w1.merchant.android.model.InitPaymentStep;
import com.w1.merchant.android.model.InitTemplatePaymentRequest;
import com.w1.merchant.android.model.PaymentDetails;
import com.w1.merchant.android.model.PaymentState;
import com.w1.merchant.android.model.Provider;
import com.w1.merchant.android.model.ProviderList;
import com.w1.merchant.android.model.Schedule;
import com.w1.merchant.android.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.model.Template;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiPayments {


    @POST("/payments")
    public void initPayment(@Body InitPaymentRequest request, Callback<InitPaymentStep> cb);

    @POST("/payments")
    public void initTemplatePayment(@Body InitTemplatePaymentRequest request, Callback<InitPaymentStep> cb);

    @PUT("/payments/{paymentId}")
    public void submitPaymentForm(@Path("paymentId") String paymentId, @Body SubmitPaymentFormRequest request,
                                  Callback<InitPaymentStep> cb);


    @PUT("/payments")
    public void submitPaymentFormExternalId(@Query("externalId") String externalId,
                                            @Body SubmitPaymentFormRequest request,
                                            Callback<InitPaymentStep> cb);

    @POST("/payments/{paymentId}/code")
    public void sendPaymentOtpCode(Callback<Void> callback);

    @GET("/payments/{paymentId}/state")
    public void getPaymentState(@Path("paymentId") String paymentId, Callback<PaymentState> cb);

    @GET("/payments/{paymentId}")
    public void getPaymentDetails(@Path("paymentId") String paymentId, Callback<PaymentDetails> cb);

    @GET("/payments")
    public void getPaymentDetailsExternalId(@Query("externalId") String externalId, Callback<PaymentDetails> cb);

    @POST("/payments/{paymentId}/check")
    public void checkPaymentForm(@Path("paymentId") String paymentId, Callback<PaymentDetails> cb);

    @GET("/payments/templates/{templateId}")
    public void getTemplate(@Path("templateId") String templateId, Callback<Template> cb);

    @GET("/payments/templates")
    public void getTemplates(Callback<Template.TempateList> cb);

    @GET("/payments/templates")
    public void getTemplates(@Query("hasSchedule") Boolean hasSchedule, Callback<Template.TempateList> cb);

    @DELETE("/payments/templates/{templateId}")
    public void deleteTemplate(@Path("templateId") String templateId, Callback<Void> cb);

    @GET("/providers")
    public void getProviders(@Query("page") int page, @Query("itemsPerPage") int itemsPerPage,
                             @Nullable @Query("providerGroupId") String providerGroupId,
                             @Nullable @Query("locationId") String locationId,
                             @Nullable @Query("searchString") String searchString,
                             @Nullable @Query("isFavourite") Boolean isFavourite,
                             Callback<ProviderList> cb
                             );

    @GET("/providers/{providerId}")
    public void getProvider(@Path("providerId") String providerId, Callback<Provider> cb);


    @POST("/tasks")
    public void createSchedule(@Body Schedule request, Callback<CreateScheduleResponse> response);

}
