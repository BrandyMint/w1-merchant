package com.w1.merchant.android.rest.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by alexey on 13.06.15.
 */
public final class ExchangeRate implements Serializable {

    /**
     * Идентификатор списываемой валюты, согласно ISO 4217
     * <p><b>Пример:</b> 643</p>
     */
    public String srcCurrencyId;

    /**
     * Идентификатор получаемой валюты, согласно ISO 4217
     * <p><b>Пример:</b> 643</p>
     */
    public String dstCurrencyId;

    /**
     * Курс обмена валют.
     *
     * <p><b>Пример:</b> 2.53047792600000000000</p>
     */
    // 9 знаков после запятой - обычное значение. Максимум вроде 20.
    public BigDecimal rate;

    /**
     * Процент комиссии за обмен валют
     * * <p><b>Пример:</b> 0.0000</p>
     */
    public BigDecimal commissionRate;

    public ExchangeRate() {
    }

    public ExchangeRate(String srcId, String dstId, BigDecimal rate, BigDecimal commissionRate) {
        this.srcCurrencyId = srcId;
        this.dstCurrencyId = dstId;
        this.rate = rate;
        this.commissionRate = commissionRate;
    }

    /**
     * @return комиссия от 0 до 1
     */
    private BigDecimal getCommissionFraction() {
        return commissionRate.movePointLeft(2);
    }

    public BigDecimal calculateExchangeFromSource(@NonNull BigDecimal srcAmount) {
        if (BigDecimal.ZERO.compareTo(rate) == 0) return srcAmount;
        return srcAmount.multiply(BigDecimal.ONE.subtract(getCommissionFraction())).divide(rate, RoundingMode.DOWN);
    }

    public BigDecimal calculateExchangeFromTarget(BigDecimal dstAmount) {
        BigDecimal divisor = BigDecimal.ONE.subtract(getCommissionFraction());
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return dstAmount;
        return rate.multiply(dstAmount).divide(divisor, RoundingMode.UP);
    }

    public static class ResponseList {

        public List<ExchangeRate> items;

    }
}
