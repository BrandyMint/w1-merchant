package com.w1.merchant.android.rest.model;

import android.support.annotation.Nullable;

public class InitPaymentRequest {

    /**
     * Идентификатор поставщика услуг
     */
    public String providerId = "";

    /**
     * Уникальный в пределах пользователя идентификатор платежа, в системе учета приложения. Необязательный параметр.
     */
    @Nullable
    public String externalId;

    public InitPaymentRequest() {
    }

    public InitPaymentRequest(String providerId) {
        this.providerId = providerId;
    }

    public InitPaymentRequest(String providerId, @Nullable String externalId) {
        this(providerId);
        this.externalId = externalId;
    }

}
