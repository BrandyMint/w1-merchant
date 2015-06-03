package com.w1.merchant.android.rest.model;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InvoiceStats {

    public List<Item> items;

    public List<PeriodPaymentType> periodPaymentTypes;

    public static class Item {

        @Nullable
        public String paymentTypeId;

        public Date date;

        public int invoiceCount;

        public BigDecimal totalAmount;

        public BigDecimal totalCommissionAmount;
    }

    public static class PeriodPaymentType {

        public String paymentTypeId;

        public String title;

    }

}
