package com.w1.merchant.android.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;
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


    public static class ResponseList {

        public List<ExchangeRate> items;

    }
}
