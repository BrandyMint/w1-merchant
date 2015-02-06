package com.w1.merchant.android.model;


import java.util.ArrayList;
import java.util.List;

public class SubmitPaymentFormRequest {

    public String formId;

    public List<Param> params;

    public static class Param {

        public String fieldId;

        public String value;

        public Param() {
        }

        public Param(String fieldId, String value) {
            this.fieldId = fieldId;
            this.value = value;
        }
    }

    public SubmitPaymentFormRequest() {
    }

    public SubmitPaymentFormRequest(String formId, List<Param> params) {
        this.formId = formId;
        this.params = new ArrayList<>(params);
    }

}
