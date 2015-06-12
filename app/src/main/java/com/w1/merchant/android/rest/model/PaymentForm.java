package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentForm implements Parcelable {

    /**
     * Идентификатор формы платежа
     */
    public String formId = "";

    /**
     * Название формы платежа
     */
    public String title = "";

    public String description = "";

    public List<PaymentFormField> fields = new ArrayList<>(0);

    /**
     * @return Споиск полей, отсортированный по {linkplain Field#tabOrder}.
     */
    public List<PaymentFormField> getSortedFieldList() {
        List<PaymentFormField> fields = new ArrayList<>(this.fields);
        Collections.sort(fields, PaymentFormField.SORT_BY_TAB_ORDER_COMPARATOR);
        return fields;
    }

    /**
     *
     * @return Это - финальная форма платежа (обычно - "проверьте введенные данные").
     * Получение данной формы означает, что все платежные поля заполнены и валидны с точки зрения
     * формата полей (но ещё не проверены у провайдера).
     */
    public boolean isFinalStep() {
        return "$Final".equalsIgnoreCase(formId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.formId);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeTypedList(fields);
    }

    public PaymentForm() {
    }

    protected PaymentForm(Parcel in) {
        this.formId = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.fields = in.createTypedArrayList(PaymentFormField.CREATOR);
    }

    public static final Parcelable.Creator<PaymentForm> CREATOR = new Parcelable.Creator<PaymentForm>() {
        public PaymentForm createFromParcel(Parcel source) {
            return new PaymentForm(source);
        }

        public PaymentForm[] newArray(int size) {
            return new PaymentForm[size];
        }
    };
}
