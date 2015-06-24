package com.w1.merchant.android.rest.model;

import junit.framework.Assert;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by alexey on 12.07.15.
 */
public class ExchangeRateTest {

    private void assertExchangeFrom(String expected, ExchangeRate rate, String input) {
        Assert.assertEquals(new BigDecimal(expected).setScale(2, RoundingMode.HALF_EVEN),
                rate.calculateExchangeFromSource(new BigDecimal(input)).setScale(2, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testCalculateExchangeFromSource() throws Exception {
        ExchangeRate rate = new ExchangeRate("643", "840", new BigDecimal("61.20198000000000000000"),
                new BigDecimal("1.0000000000"));
        assertExchangeFrom("0.00", rate, "0");
        assertExchangeFrom("0.00", rate, "0.3");
        assertExchangeFrom("0.01", rate, "0.4");
        assertExchangeFrom("0.89", rate, "55.32");
        assertExchangeFrom("0.90", rate, "55.33");
        assertExchangeFrom("0.93", rate, "57.6");
        assertExchangeFrom("1.62", rate, "100");
        assertExchangeFrom("161.76", rate, "10000");
    }

    private void assertExchangeFromTarget(String expected, ExchangeRate rate, String input) {
        Assert.assertEquals(new BigDecimal(expected).setScale(2, RoundingMode.HALF_EVEN),
                rate.calculateExchangeFromTarget(new BigDecimal(input)).setScale(2, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testCalculateExchangeFromTarget() throws Exception {
        ExchangeRate rate = new ExchangeRate("643", "840", new BigDecimal("61.20198000000000000000"),
                new BigDecimal("1.0000000000"));
        assertExchangeFromTarget("0", rate, "0.00");
        assertExchangeFromTarget("0.62", rate, "0.01");
        assertExchangeFromTarget("11.75", rate, "0.19");
        assertExchangeFromTarget("6188.20", rate, "100.10");
        assertExchangeFromTarget("61820.18", rate, "1000");
        assertExchangeFromTarget("618201.82", rate, "10000");
    }
}