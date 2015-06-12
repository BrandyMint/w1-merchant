package com.w1.merchant.android.rest.model;

import android.graphics.Color;

import java.util.Date;
import java.util.List;

/**
 * Created by alexey on 13.06.15.
 */
public class ExternalAccount {

    /**
     * <p><b>Пример: </b>1000001</p>
     */
    public long externalAccountId;

    public Date createDate;

    /**
     * <p><b>Пример: </b>"Название"</p>
     */
    public String title;

    /**
     * <p><b>Пример: </b>"410048203204732934"</p>
     */
    public String masterField;

    public Date expireDate;

    /**
     * <p><b>Пример: </b>"CreditCardRUB"</p>
     */
    public String paymentTypeId;

    public Date lastUsedDate;

    /**
     * Цвет привязанного инструмента
     * * <p><b>Пример: </b>"FACE8D"</p>
     */
    public String color;

    public String imageId;

    public String currencyId;

    public boolean isDefault;

    public static class ResponseList {
        public List<ExternalAccount> items;
    }

    /**
     * @return {@link #color} в int. Если не удается отпарсить, то {@link Color#BLUE}.
     */
    public int getColor() {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException ignore) {
            return Color.BLUE;
        }
    }

}
