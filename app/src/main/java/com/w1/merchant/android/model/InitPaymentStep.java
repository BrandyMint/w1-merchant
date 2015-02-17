package com.w1.merchant.android.model;


import java.math.BigInteger;

public class InitPaymentStep {

    /**
     * Идентификатор платежа, назначенный сервисом
     */
    public BigInteger paymentId;

    /**
     * Идентификатор поставщика услуг
     */
    public String providerId;

    /**
     * Состояние
     */
    public PaymentState state;

    /**
     * Номер шага
     */
    public int step;

    /**
     * Общее количество шагов
     */
    public int stepCount;

    /**
     * Идентификатор основного поля, требующего заполнения
     */
    public String masterFieldId;


    public PaymentForm form;

}
