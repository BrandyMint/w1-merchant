package com.w1.merchant.android.model;

import java.math.BigDecimal;

public class Provider {

    /**
     * Идентификатор поставщика услуг
     */
    public String providerId;

    /**
     * Идентификатор пользователя, соответствующий поставщику
     */
    public String userId;

    /**
     * Название поставщика услуг
     */
    public String title;

    /**
     * Описание поставщика услуг
     */
    public String description;

    /**
     * URL поставщика услуг
     */
    public String url;

    /**
     * Идентификатор валюты, согласно ISO 4217
     */
    public String currencyId;

    /**
     * Минимальная допустимая сумма платежа
     */
    public BigDecimal minAmount;

    /**
     * Максимальная допустимая сумма платежа
     */
    public BigDecimal maxAmount;

    /**
     * Максимальная допустимая сумма платежа с учетом всех текущих лимитов кошелька
     */
    public BigDecimal maxPaymentAmount;

    /**
     * Флаг, показывающий, добавлен ли поставщик в Избранное
     */
    public boolean isFavourite;

    /**
     * Флаг, показывающий, что поставщик услуг включен
     */
    public boolean isActive;

    /**
     * Флаг, показывающий, разрешено ли оплачивать услуги поставщика “крашеными” деньгами
     */
    public boolean isSafe;

    /**
     * Название основного платежного поля
     */
    public String masterFieldTitle;

    /**
     * Ссылка на логотип
     */
    public String logoUrl;

    /**
     * Описание комиссии
     */
    public Commission commission = Commission.DUMMY;

    public static class Commission {

        public static final Commission DUMMY = new Commission();

        public BigDecimal rate;

        public BigDecimal cost;

        public BigDecimal min;

        public BigDecimal max;

        public boolean hasMin() {
            return BigDecimal.ZERO.compareTo(min) != 0;
        }

        public boolean hasMax() {
            return BigDecimal.ZERO.compareTo(max) != 0;
        }

    }

    public BigDecimal getCommisson(BigDecimal sum) {
        BigDecimal commission = this.commission.cost.add(sum.multiply(this.commission.rate).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP));

        if (this.commission.hasMin()) commission = commission.max(this.commission.min);
        if (this.commission.hasMax()) commission = commission.min(this.commission.max);

        return commission.setScale(2, BigDecimal.ROUND_UP);
    }

    public BigDecimal getSumWithCommission(BigDecimal sum) {
        return sum.add(getCommisson(sum));
    }

    public BigDecimal getMinAmountWithComission() {
        return minAmount.add(getCommisson(minAmount));
    }

    public BigDecimal getMaxAmountWithComission() {
        return maxAmount.add(getCommisson(maxAmount));
    }
}
