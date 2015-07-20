package com.w1.merchant.android.utils;

import com.w1.merchant.android.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import static com.w1.merchant.android.utils.TextUtilsW1.cleanupPhoneNumber;
import static com.w1.merchant.android.utils.TextUtilsW1.formatUserId;
import static com.w1.merchant.android.utils.CurrencyHelper.parseAmount;
import static com.w1.merchant.android.utils.TextUtilsW1.preparePhoneInternationalFormat;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class TextUtilsW1Test {

    @Test
    public void testFormatUserId() throws Exception {
        assertEquals("", formatUserId(null));
        assertEquals("", formatUserId(""), "");
        assertEquals("1", formatUserId("1"));
        assertEquals("12", formatUserId("12"));
        assertEquals("123", formatUserId("123"));
        assertEquals("1\u2009234", formatUserId("1234").toString());
        assertEquals("12\u2009345", formatUserId("12345").toString());
        assertEquals("123\u2009456", formatUserId("123456").toString());
        assertEquals("123\u2009456", formatUserId("123456").toString());
        assertEquals("1\u2009234\u2009567", formatUserId("1234567").toString());

        assertEquals("blabla", formatUserId("blabla"));
        assertEquals("123blabla", formatUserId("123blabla"));
        assertEquals("123 ", formatUserId("123 "));
        assertEquals("1234 blabla", formatUserId("1234 blabla"));
    }

    @Test
    public void testCleanupNumber() throws Exception {
        assertEquals("", cleanupPhoneNumber("   "));
        assertEquals("user@localhost", cleanupPhoneNumber("   user@localhost    "));
        assertEquals("89053735703", cleanupPhoneNumber("   8  ( 905 ) 373 - 57 - 03 "));
    }

    @Test
    public void testPreparePhoneInternationalFormat() throws Exception {
        assertEquals("+79053735703", preparePhoneInternationalFormat("89053735703"));
        assertEquals("+79053735703", preparePhoneInternationalFormat("79053735703"));
        assertEquals("+79053735703", preparePhoneInternationalFormat("+79053735703"));
    }

    @Test
    public void testParseAmount() throws Exception {
        assertNull(parseAmount(null));
        assertNull(parseAmount(""));
        assertNull(parseAmount("."));
        assertNull(parseAmount("dsf"));
        assertEquals(BigDecimal.ZERO, parseAmount("0"));
        assertEquals(BigDecimal.ZERO.compareTo(parseAmount("0,0")), 0);
        assertEquals(BigDecimal.ZERO.compareTo(parseAmount("000.00 ")), 0);
        assertEquals(0, BigDecimal.ONE.compareTo(parseAmount(" 001.00 ")));
        assertEquals(0, BigDecimal.ONE.compareTo(parseAmount("1")));
        assertEquals(BigDecimal.valueOf(1.23), parseAmount("1,23"));
    }
}