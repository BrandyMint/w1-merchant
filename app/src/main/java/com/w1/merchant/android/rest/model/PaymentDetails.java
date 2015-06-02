package com.w1.merchant.android.rest.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class PaymentDetails {

    /**
     * Идентификатор платежа, назначенный сервисом
     */
    public BigInteger paymentId;

    /**
     * Идентификатор платежа, назначенный пользователем
     */
    public String externalId;

    /**
     * Сумма к зачислению
     */
    public BigDecimal amount;

    /**
     * Сумма комиссии платежа
     */
    public BigDecimal commissionAmount;

    /**
     * Идентификатор валюты, согласно ISO 4217
     */
    public String currencyId;

    /**
     * Состояние платежа
     */
    public PaymentState state;

    /**
     * Дата создания платежа
     */
    public Date createDate;

    /**
     * Дата последнего изменения платежа
     */
    public Date updateDate;

    /**
     * Идентификатор соответствующего платежу перевода
     */
    public String transferId;

    /**
     * Идентификатор поля, значение которого является уникальным идентификатором пользователя со стороны провайдера
     */
    public String masterField;

    /**
     * Поставщик услуг, в пользу которого совершается или совершен платеж
     */
    public Provider provider;

    /**
     * Параметры платежа
     */
    public List<Param> params;

    public static class Param {

        /**
         * Идентификатор поля платежа
         */
        public String fieldId;

        /**
         * Название поля платежа
         */
        public String fieldTitle;

        /**
         * Значение поля платежа
         */
        public String value;

        /**
         * Тип поля платежа
         */
        public String fieldType;

        /**
         * Название элемента списка платежа
         */
        public String ListItemTitle;
    }
}
