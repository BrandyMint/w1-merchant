package com.w1.merchant.android.rest.model;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by alexey on 15.02.15.
 */
public class InvoiceRequest {

    public String currencyId;

    public BigDecimal amount;

    public String description = "";

    @Nullable
    public Date expireDate;

    @Nullable
    public String orderId;

    public String recipient;

    public InvoiceRequest(String recipient, BigDecimal amount, String description, String currencyId) {
        this.recipient = recipient;
        this.amount = amount;
        this.description = description;
        this.currencyId = currencyId;
    }

}
