package com.w1.merchant.android.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.Invoice;
import com.w1.merchant.android.model.InvoiceRequest;
import com.w1.merchant.android.model.InvoiceStats;
import com.w1.merchant.android.model.Invoices;

import java.util.Date;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface ApiInvoices {

    @POST("/invoices")
    public Observable<Invoice> createInvoice(@Body InvoiceRequest request);

    @GET("/invoices/{invoiceId}")
    public Observable<Invoice> getInvoice(@Path("invoiceId") String invoiceId);

    @GET("/invoices")
    public Observable<Invoice> getInvoiceByOrderId(@Query("orderId") String orderId);

    @GET("/invoices")
    public Observable<Invoices> getInvoices(@Query("pageNumber") int pageNumber,
                            @Query("itemsPerPage") int itemsPerPage,
                            @Nullable @Query("invoiceStateId") String invoiceStateId,
                            @Nullable @Query("direction") String direction,
                            @Nullable @Query("fromCreateDate") Date fromCreateDate,
                            @Nullable @Query("toCreateDate") Date toCreateDate,
                            @Nullable @Query("searchString") String searchString);

    @GET("/invoices/statistics")
    public Observable<InvoiceStats> getStats(String currencyId, Date fromDate, Date toDate);


}
