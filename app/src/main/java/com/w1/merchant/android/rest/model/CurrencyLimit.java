package com.w1.merchant.android.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Лимиты пользователя по валютам
 */
public class CurrencyLimit  implements Serializable {

    public static final String TYPE_BALANCE = "Balance";

    public static final String TYPE_CREDIT = "Credit";

    public static final String TYPE_DEBIT = "Debit";

    public static final String CONSTRAINT_MAX_AMOUNT = "MaxAmount";

    public static final String CONSTRAINT_MAX_DAY_AMOUNT = "MaxDayAmount";

    public static final String CONSTRAINT_MAX_MONTH_AMOUNT = "MaxMonthAmount";

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
