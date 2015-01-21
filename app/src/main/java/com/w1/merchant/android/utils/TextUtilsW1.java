package com.w1.merchant.android.utils;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;

import java.util.Locale;

public final class TextUtilsW1 {

    private static final Spanned SPANNED_EMPTY = new SpannedString("");

    private TextUtilsW1() {}

    public static boolean isBlank(CharSequence text) {
        return text == null || android.text.TextUtils.isEmpty(removeTrailingWhitespaces(text));
    }

    public static String capitalize(String text) {
        if (text == null) return "";
        return text.substring(0,1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    /**
     * Безопасный вариант {@linkplain android.text.Html#toHtml(android.text.Spanned)}
     * При ошибках возвращается cs.toString(),
     */
    public static String safeToHtml(CharSequence cs) {
        Spanned spanned;
        if (android.text.TextUtils.isEmpty(cs)) return "";
        if (cs instanceof  Spanned) {
            spanned = (Spanned)cs;
        } else {
            spanned = new SpannedString(cs);
        }

        return Html.toHtml(spanned);
    }

    public static Spanned safeFromHtmlPreLine(@Nullable String source, @Nullable Html.ImageGetter imageGetter) {
        if (TextUtils.isEmpty(source)) return SPANNED_EMPTY;
        return Html.fromHtml(source.replace("\n", "<br/>"), imageGetter, null);
    }

    /**
     * Удаление висящих пробелов и переновов строк.
     * Работает для большей части CharSequence ({@linkplain android.text.Spannable},
     * {@linkplain android.text.Spanned}, {@linkplain String}, и т.п.)
     */
    public static CharSequence removeTrailingWhitespaces(CharSequence source) {
        int origLength, length;
        if (source == null) return null;
        origLength = source.length();
        if (origLength == 0) return source;

        length = origLength;
        while (length > 0 && Character.isWhitespace(source.charAt(length - 1))) {
            length -= 1;
        }

        if (origLength == length) {
            return source;
        } else {
            return source.subSequence(0, length);
        }
    }
}
