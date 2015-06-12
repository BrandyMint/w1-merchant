package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Элемент списка в списковых полях
 */
public class PaymentFormListFieldItem implements Parcelable {

    public String title;

    public String value;

    public boolean isSelected;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.value);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }

    public PaymentFormListFieldItem() {
    }

    protected PaymentFormListFieldItem(Parcel in) {
        this.title = in.readString();
        this.value = in.readString();
        this.isSelected = in.readByte() != 0;
    }

    public static final Creator<PaymentFormListFieldItem> CREATOR = new Creator<PaymentFormListFieldItem>() {
        public PaymentFormListFieldItem createFromParcel(Parcel source) {
            return new PaymentFormListFieldItem(source);
        }

        public PaymentFormListFieldItem[] newArray(int size) {
            return new PaymentFormListFieldItem[size];
        }
    };
}
