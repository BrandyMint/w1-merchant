package com.w1.merchant.android.rest.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * детали операции обмена валюты
 */
public class ExchangeRateStatus {

    /**
     * Внутренний идентификатор операции в системе
     */
    public long operationId;

    /**
     * Дата создания операции
     */
    public Date updateDate;


    public Date createDate;

    /**
     * Состояние операции
     * * * <p><b>Пример:</b> "Accepted"</p>
     */
    public String operationStateId;

    /**
     * Курс, по которому произведен обмен
     * * * * <p><b>Пример:</b> 3.8234</p>
     */
    public BigDecimal rate;

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
     * <p><b>Пример:</b>  7.0</p>
     */
    public BigDecimal srcAmount;

    /**
     * Сумма в получаемой валюте
     * <p><b>Пример:</b> 9.0</p>
     */
    public BigDecimal dstAmount;

    /**
     * Необязательно: внешний уникальный идентификатор операции
     * * <p><b>Пример:</b> 123</p>
     */
    public Long externalId;

}
