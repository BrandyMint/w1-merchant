package com.w1.merchant.android.service;

import android.support.annotation.Nullable;

import com.w1.merchant.android.model.Invoice;
import com.w1.merchant.android.model.InvoiceRequest;
import com.w1.merchant.android.model.InvoiceStats;
import com.w1.merchant.android.model.Invoices;

import java.util.Date;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiInvoices {

    @POST("/invoices")
    public void createInvoice(@Body InvoiceRequest request, Callback<Invoice> cb);

    @GET("/invoices/{invoiceId}")
    public void getInvoice(@Path("invoiceId") String invoiceId, Callback<Invoice> cb);

    @GET("/invoices")
    public void getInvoiceByOrderId(@Query("orderId") String orderId, Callback<Invoice> cb);

    @GET("/invoices")
    public void getInvoices(@Query("pageNumber") int pageNumber,
                            @Query("itemsPerPage") int itemsPerPage,
                            @Nullable @Query("invoiceStateId") String invoiceStateId,
                            @Nullable @Query("direction") String direction,
                            @Nullable @Query("fromCreateDate") Date fromCreateDate,
                            @Nullable @Query("toCreateDate") Date toCreateDate,
                            @Nullable @Query("searchString") String searchString,
                            Callback<Invoices> cb
    );

    @GET("/invoices/statistics")
    public void getStats(String currencyId, Date fromDate, Date toDate, Callback<InvoiceStats> stats);


}
