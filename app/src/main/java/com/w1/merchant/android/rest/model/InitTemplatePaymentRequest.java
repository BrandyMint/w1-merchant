package com.w1.merchant.android.rest.model;

import android.support.annotation.Nullable;

public class InitTemplatePaymentRequest {

    /**
     * Идентификатор поставщика услуг
     */
    public String templateId = "";

    /**
     * Уникальный в пределах пользователя идентификатор платежа, в системе учета приложения. Необязательный параметр.
     */
    @Nullable
    public String externalId;

    public InitTemplatePaymentRequest() {
    }

    public InitTemplatePaymentRequest(String templateId) {
        this.templateId = templateId;
    }

    public InitTemplatePaymentRequest(String templateId, @Nullable String externalId) {
        this(templateId);
        this.externalId = externalId;
    }

}
