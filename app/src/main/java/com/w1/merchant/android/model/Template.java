package com.w1.merchant.android.model;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class Template {

    public BigInteger templateId;

    public String title;

    public String providerId;

    public String providerTitle;

    public String providerLogoUrl;

    public BigInteger prototypePaymentId;

    public String masterField;

    public BigDecimal amount;

    public String currencyId;

    public Date lastUseDate;

    public boolean hasSchedule;

    @Nullable
    public Schedule schedule;

    @Nullable
    public List<Field> fields;

    public static class Field {

        public String fieldId;

        public String fieldTitle;

        public int tabOrder;

        public String fieldValue;
    }

    public static final class TempateList {

        public List<Template> items;

    }

}
