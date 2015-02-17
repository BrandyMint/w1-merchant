package com.w1.merchant.android.utils;

import org.junit.Test;

import static com.w1.merchant.android.utils.TextUtilsW1.formatUserId;
import static junit.framework.Assert.assertEquals;

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
}