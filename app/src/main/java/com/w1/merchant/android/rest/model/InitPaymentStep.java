package com.w1.merchant.android.rest.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigInteger;

public class InitPaymentStep implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.paymentId);
        dest.writeString(this.providerId);
        dest.writeParcelable(this.state, 0);
        dest.writeInt(this.step);
        dest.writeInt(this.stepCount);
        dest.writeString(this.masterFieldId);
        dest.writeParcelable(this.form, 0);
    }

    public InitPaymentStep() {
    }

    protected InitPaymentStep(Parcel in) {
        this.paymentId = (BigInteger) in.readSerializable();
        this.providerId = in.readString();
        this.state = in.readParcelable(PaymentState.class.getClassLoader());
        this.step = in.readInt();
        this.stepCount = in.readInt();
        this.masterFieldId = in.readString();
        this.form = in.readParcelable(PaymentForm.class.getClassLoader());
    }

    public static final Parcelable.Creator<InitPaymentStep> CREATOR = new Parcelable.Creator<InitPaymentStep>() {
        public InitPaymentStep createFromParcel(Parcel source) {
            return new InitPaymentStep(source);
        }

        public InitPaymentStep[] newArray(int size) {
            return new InitPaymentStep[size];
        }
    };
}
