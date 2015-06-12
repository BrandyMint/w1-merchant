package com.w1.merchant.android.rest.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubmitPaymentFormRequest implements Serializable, Parcelable {

    public String formId;

    public List<Param> params;

    public static SubmitPaymentFormRequest createTemplateRequest(String paymentId, @Nullable String templateId, String templateName) {
        List<Param> params = new ArrayList<>(3);
        params.add(new Param("Title", templateName));
        params.add(new Param("PaymentId", paymentId));
        if (!TextUtils.isEmpty(templateId)) params.add(new Param("TemplateId", templateId));
        return new SubmitPaymentFormRequest("Index", params);
    }

    /**
     * Значение первого найденного поля
     */
    @Nullable
    public String findParamValue(String fieldId) {
        for (Param param: params) {
            if (fieldId.equals(param.fieldId)) return param.value;
        }
        return null;
    }

    public static final class Param  implements Serializable, Parcelable {

        public String fieldId;

        public String value;

        public Param() {
        }

        public Param(String fieldId, String value) {
            this.fieldId = fieldId;
            this.value = value;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.fieldId);
            dest.writeString(this.value);
        }

        protected Param(Parcel in) {
            this.fieldId = in.readString();
            this.value = in.readString();
        }

        public static final Parcelable.Creator<Param> CREATOR = new Parcelable.Creator<Param>() {
            public Param createFromParcel(Parcel source) {
                return new Param(source);
            }

            public Param[] newArray(int size) {
                return new Param[size];
            }
        };
    }

    public SubmitPaymentFormRequest() {
    }

    public SubmitPaymentFormRequest(String formId, List<Param> params) {
        this.formId = formId;
        this.params = new ArrayList<>(params);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.formId);
        dest.writeTypedList(params);
    }

    protected SubmitPaymentFormRequest(Parcel in) {
        this.formId = in.readString();
        this.params = in.createTypedArrayList(Param.CREATOR);
    }

    public static final Parcelable.Creator<SubmitPaymentFormRequest> CREATOR = new Parcelable.Creator<SubmitPaymentFormRequest>() {
        public SubmitPaymentFormRequest createFromParcel(Parcel source) {
            return new SubmitPaymentFormRequest(source);
        }

        public SubmitPaymentFormRequest[] newArray(int size) {
            return new SubmitPaymentFormRequest[size];
        }
    };
}
