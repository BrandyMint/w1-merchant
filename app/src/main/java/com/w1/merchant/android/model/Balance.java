package com.w1.merchant.android.model;

import java.math.BigDecimal;

public class Balance {

    public String currencyId;

    public BigDecimal amount;

    public BigDecimal safeAmount;

    public BigDecimal holdAmount;

    public BigDecimal overdraft;

    public BigDecimal availableAmount;

    public BigDecimal overLimitAmount;

    public String visibilityType;

    public boolean isAccountIdentified;

    public int identificationLevel;

    public boolean isNative;


}
