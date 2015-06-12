package com.w1.merchant.android.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Balance implements Serializable {

    public static final String VISIBILITY_TYPE_UNDEFINED = "Undefined";

    public static final String VISIBILITY_TYPE_ALWAYS = "Always";

    /**
     * Идентификатор валюты, согласно ISO 4217:
     * <p><b>Пример: </b>643</p>
     */
    public String currencyId;

    /**
     *  Остаток
     *  <p><b>Пример: </b>6.0000</p>
     */
    public BigDecimal amount;

    /**
     * Окрашенная часть остатка, на которую распространяются определенные ограничения
     * <p><b>Пример: </b>0.0000</p>
     */
    public BigDecimal safeAmount;

    /**
     * Часть остатка, заблокированная на время совершения какой-либо расходной операции
     * <p><b>Пример: </b>0.0000</p>
     */
    public BigDecimal holdAmount;

    /**
     * Доступный размер кредитного лимита
     * <p><b>Пример: </b>0.0000</p>
     */
    public BigDecimal overdraft;

    /**
     * Сумма, доступная для совершения расходных операций
     * <p><b>Пример: </b>6.0000</p>
     */
    public BigDecimal availableAmount;

    public BigDecimal overLimitAmount;

    public String visibilityType;

    public boolean isAccountIdentified;

    public int identificationLevel;

    /**
     * Является ли данная валюта основной
     */
    public boolean isNative;

    public boolean isVisible() {
        return !VISIBILITY_TYPE_UNDEFINED.equalsIgnoreCase(visibilityType);
    }

}
