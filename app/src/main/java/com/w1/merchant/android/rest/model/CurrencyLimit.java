package com.w1.merchant.android.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Лимиты пользователя по валютам
 */
public class CurrencyLimit  implements Serializable {

    public String currencyId;

    public String type;

    public List<Constraint> constraints;

    public static class Constraint implements Serializable {

        public String name;

        public BigDecimal value;

        public BigDecimal usedValue;

        public BigDecimal holdValue;

    }

}
