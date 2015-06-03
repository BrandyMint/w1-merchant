package com.w1.merchant.android.rest.model;

/**
* Created by alexey on 14.02.15.
*/
public class PaymentState {

    /**
     * платеж создан (идентификатор зарезервирован)
     */
    public static final String PAYMENT_STATE_CREATED = "Created";

    /**
     * платеж изменен (внесены данные одного или нескольких шагов, актуально для многошаговых провайдеров)
     */
    public static final String PAYMENT_STATE_UPDATED = "Updated";

    /**
     * платеж блокирован системой.
     */
    public static final String PAYMENT_STATE_BLOCKED = "Blocked";

    /**
     * платеж в находится в обработке сервером ЕК.
     */
    public static final String PAYMENT_STATE_PROCESSING = "Processing";

    /**
     * платеж проходит проверку на стороне провайдера.
     */
    public static final String PAYMENT_STATE_CHECKING = "Checking";

    /**
     * платеж в обработке на стороне провайдера.
     */
    public static final String PAYMENT_STATE_PAYING = "Paying";

    /**
     * при обработке платежа произошли ошибка.
     */
    public static final String PAYMENT_STATE_PROCESS_ERROR = "ProcessError";

    /**
     * ошибка проверки платежа.
     */
    public static final String PAYMENT_STATE_CHECK_ERROR = "CheckError";

    /**
     * ошибка проведения платежа на стороне провайдера.
     */
    public static final String PAYMENT_STATE_PAY_ERROR = "PayError";

    /**
     *  платеж отменен.
     */
    public static final String PAYMENT_STATE_PAY_CANCELLED = "Canceled";

    /**
     * платеж проведен успешно.
     */
    public static final String PAYMENT_STATE_PAY_PAID = "Paid";


    public String stateId;

    public String description;

    /**
     * @return платеж в одном из конечных состояний
     */
    public boolean isFinished() {
        switch (stateId) {
            case PAYMENT_STATE_PROCESS_ERROR:
            case PAYMENT_STATE_CHECK_ERROR:
            case PAYMENT_STATE_PAY_ERROR:
            case PAYMENT_STATE_PAY_CANCELLED:
            case PAYMENT_STATE_PAY_PAID:
                return true;
            default:
                return false;
        }
    }

}
