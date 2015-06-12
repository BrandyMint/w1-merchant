package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
* Created by alexey on 14.02.15.
*/
public class PaymentState implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.stateId);
        dest.writeString(this.description);
    }

    public PaymentState() {
    }

    protected PaymentState(Parcel in) {
        this.stateId = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<PaymentState> CREATOR = new Parcelable.Creator<PaymentState>() {
        public PaymentState createFromParcel(Parcel source) {
            return new PaymentState(source);
        }

        public PaymentState[] newArray(int size) {
            return new PaymentState[size];
        }
    };
}
