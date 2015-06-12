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


    /**
     * Инициализация платежа
     */
    @POST("/payments")
    Observable<InitPaymentStep> initPayment(@Body InitPaymentRequest request);

    /**
     * Инициализация платежа с помощью шаблона
     */
    @POST("/payments")
    Observable<InitPaymentStep> initTemplatePayment(@Body InitTemplatePaymentRequest request);

    /**
     * Заполнение формы платежа
     */
    @PUT("/payments/{paymentId}")
    Observable<InitPaymentStep> submitPaymentForm(@Path("paymentId") String paymentId, @Body SubmitPaymentFormRequest request);


    /**
     * Заполнение формы платежа с использованием идентификатора, назначенного пользователем (externalId)
     * @param externalId
     * @param request
     * @return
     */
    @PUT("/payments")
    Observable<InitPaymentStep> submitPaymentFormExternalId(@Query("externalId") String externalId,
                                            @Body SubmitPaymentFormRequest request);

    /**
     * Для сохранения шаблона использовать providerId="SaveTemplate", параметрами передавать
     * {"FieldId":"Title","Value":"имя шаблона"} и {"FieldId":"PaymentId","Value":"ID платежа"}
     */
    @PUT("/payments")
    Observable<InitPaymentStep> submitPaymentFormProviderId(@Query("providerId") String providerId,
                                                            @Body SubmitPaymentFormRequest request);


    /**
     * Получение кода подтверждения платежа
     * В зависимости от настройки, для проведения платежа может быть необходим одноразовый код,
     * который в этом случае запрашивается на последней форме (скалярное поле с идентификатором
     * "$OtpCode"). Указанный запрос инициирует отправку кода подтверждения платежа на мобильный
     * телефон пользователя.
     */
    @POST("/payments/{paymentId}/code")
    Observable<Void> sendPaymentOtpCode();

    /**
     * Получение состояния платежа
     * @param paymentId
     * @return
     */
    @GET("/payments/{paymentId}/state")
    Observable<PaymentState> getPaymentState(@Path("paymentId") String paymentId);

    /**
     * Получение деталей платежа
     * @return
     */
    @GET("/payments/{paymentId}")
    Observable<PaymentDetails> getPaymentDetails(@Path("paymentId") String paymentId);

    /**
     *  Получение деталей платежа по идентификатору, назначенному пользователем (externalId)
     * @param externalId
     * @return
     */
    @GET("/payments")
    Observable<PaymentDetails> getPaymentDetailsExternalId(@Query("externalId") String externalId);

    /**
     * Проверка данных платежа
     * Метод проверки платежа вызывается после получения финальной формы платежа (идентификатор
     * формы - “$Final”). Получение данной формы означает, что все платежные поля заполнены и
     * валидны с точки зрения формата полей.
     * В случае, если платеж не найден, либо не все обязательные платежные поля были заполнены,
     * возвращает 404 NotFound.
     */
    @POST("/payments/{paymentId}/check")
    Observable<PaymentDetails> checkPaymentForm(@Path("paymentId") String paymentId);



    @GET("/payments/templates/{templateId}")
    Observable<Template> getTemplate(@Path("templateId") String templateId);

    @GET("/payments/templates")
    Observable<Template.TempateList> getTemplates();

    @GET("/payments/templates")
    Observable<Template.TempateList> getTemplates(@Query("hasSchedule") Boolean hasSchedule);

    @DELETE("/payments/templates/{templateId}")
    Observable<Void> deleteTemplate(@Path("templateId") String templateId);

    @GET("/providers")
    Observable<ProviderList> getProviders(@Query("page") int page, @Query("itemsPerPage") int itemsPerPage,
                             @Nullable @Query("providerGroupId") String providerGroupId,
                             @Nullable @Query("locationId") String locationId,
                             @Nullable @Query("searchString") String searchString,
                             @Nullable @Query("isFavourite") Boolean isFavourite);

    @GET("/providers/{providerId}")
    Observable<Provider> getProvider(@Path("providerId") String providerId);


    @POST("/tasks")
    Observable<CreateScheduleResponse> createSchedule(@Body Schedule request);

}
