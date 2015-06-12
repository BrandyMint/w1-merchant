package com.w1.merchant.android.rest.model;

import java.math.BigDecimal;

/**
 * Запрос операции конвертации валют
 */
public class ExchangeRequest {

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


    public BigDecimal srcAmount;

    /**
     * Сумма в получаемой валюте
     * <p><b>Пример:</b> 1.23</p>
     */
    public BigDecimal dstAmount;

    /**
     * Необязательно: внешний уникальный идентификатор операции
     * * <p><b>Пример:</b> 123</p>
     */
    public Long externalId;

    public ExchangeRequest() {
    }

    public ExchangeRequest newInstance(String srcCurrencyId, String dstCurrencyId, BigDecimal dstAmount) {
        ExchangeRequest req = new ExchangeRequest();
        req.srcCurrencyId = srcCurrencyId;
        req.dstCurrencyId = dstCurrencyId;
        req.dstAmount = dstAmount;
        return req;
    }

    public ExchangeRequest newInstanceSrcAmount(String srcCurrencyId, String dstCurrencyId, BigDecimal srcAmount) {
        ExchangeRequest req = new ExchangeRequest();
        req.srcCurrencyId = srcCurrencyId;
        req.dstCurrencyId = dstCurrencyId;
        req.srcAmount = srcAmount;
        return req;
    }

}
