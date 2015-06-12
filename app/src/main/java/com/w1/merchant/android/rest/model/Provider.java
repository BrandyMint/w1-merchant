package com.w1.merchant.android.rest.model;

import android.content.res.Resources;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class Provider implements Serializable {

    /**
     * Идентификатор поставщика услуг
     */
    public String providerId = "";

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

    public static class SortByTitleComparator implements Comparator<Provider> {

        private final Collator mCollator;

        public SortByTitleComparator() {
            mCollator = Collator.getInstance(Locale.getDefault());
            mCollator.setStrength(Collator.TERTIARY);
        }

        @Override
        public int compare(Provider lhs, Provider rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            } else if (lhs == null || lhs.title == null) {
                return -1;
            } else if (rhs == null || rhs.title == null) {
                return 1;
            } else {
                int textCompare =  mCollator.compare(lhs.title, rhs.title);
                return textCompare == 0 ? lhs.providerId.compareTo(rhs.providerId) : textCompare;
            }
        }
    }

    public final static class Commission implements Serializable {

        public static final Commission DUMMY = new Commission();

        /**
         * Комиссия в процентах. Напр. 3
         */
        public BigDecimal rate;

        /**
         * Добавочная стоимость
         */
        public BigDecimal cost;

        /**
         * Минимальная сумма
         */
        public BigDecimal min;

        public BigDecimal max;

        public boolean hasMin() {
            return BigDecimal.ZERO.compareTo(min) != 0;
        }

        public boolean hasMax() {
            return BigDecimal.ZERO.compareTo(max) != 0;
        }

        public boolean hasAdditionalCost() {
            return BigDecimal.ZERO.compareTo(cost) < 0;
        }

        public boolean hasPctRate() {
            return BigDecimal.ZERO.compareTo(rate) < 0;
        }

        public Commission() {
        }

        public BigDecimal calc(BigDecimal sum) {
            //  commission.cost + (sum * commission.rate / 100)
            BigDecimal amount1 = sum.multiply(rate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
            BigDecimal commission = cost.add(amount1).setScale(2, RoundingMode.HALF_UP);

            if (hasMin()) commission = commission.max(min);
            if (hasMax()) commission = commission.min(max);

            return commission;
        }

        public boolean isZero() {
            return !hasPctRate() && !hasAdditionalCost();
        }

        public CharSequence getDescription(String currencyId, Resources resources) {
            return TextUtilsW1.formatCommission(this, currencyId, resources);
        }


    }

    public BigDecimal getCommission(BigDecimal sum) {
        return this.commission.calc(sum);
    }

    public BigDecimal getSumWithCommission(BigDecimal sum) {
        return sum.add(getCommission(sum));
    }

    public BigDecimal getMinAmountWithComission() {
        return minAmount.add(getCommission(minAmount));
    }

    public BigDecimal getMaxAmountWithComission() {
        return maxAmount.add(getCommission(maxAmount));
    }

    /**
     * @return URL на логотип нормального размера (не logoUrl)
     */
    public String getLogoUrl() {
        return String.format(Locale.US, Constants.URL_PROVIDER_LOGO, providerId);
    }

}
