package com.w1.merchant.android.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    /**
     * @param currencyId
     * @param weight Толщина для символа рубля от 0 до 2
     * @return
     */
    public static Spanned getCurrencySymbol2(String currencyId, int weight) {
        if (currencyId.equals("643")) {
            return getRoubleSymbol(weight);
        } else {
            return new SpannedString(getCurrencySymbol(currencyId));
        }
    }

    @Nullable
    public static String getIso4217CodeByNumber(String number) {
        switch (number) {
            case "398": return "KZT";
            case "643": return "RUB";
            case "710": return "ZAR";
            case "840": return "USD";
            case "972": return "TJS";
            case "974": return "BYR";
            case "978": return "EUR";
            case "980": return "UAH";
            case "981": return "GEL";
            case "985": return "PLN";
            default: return null;
        }
    }

    public static String getCurrencySymbol(String currencyId) {
        String result;
        Exception exception = null;
        try {
            String iso4217Code = getIso4217CodeByNumber(currencyId);
            if (iso4217Code != null && !"RUB".equals(iso4217Code)) {
                result =  Currency.getInstance(iso4217Code).getSymbol(Locale.getDefault());
                if (!TextUtils.equals(result, iso4217Code)) return result;
            }
        } catch (Exception e) {
            exception = e;
        }

        switch (currencyId) {
            case "398":  //казах
                result = "₸";//KZTU+20B8&#8376
                break;
            case "643":  //рубль
                result = "RUB";
                break;
            case "710": //южноафр
                result = "R";//ZARR
                break;
            case "840": //USD
                result = "$";//USD
                break;
            case "972": //таджик
                result = "смн.";//TJSсмн.
                break;
            case "974": //белорус
                result = "Br";//BrBYR
                break;
            case "978": //EUR
                result = "€";//EURU+20AC&#8364
                break;
            case "980": //укр
                result = "₴";//UAHU+20B4&#8372
                break;
            case "981": //Груз.
                result = "lari";//GEL U+20BE
                break;
            case "985": //польск
                result = "zł";//PLN
                break;
            default: //?
                result = "?";
                if (BuildConfig.DEBUG)
                    Log.v(Constants.LOG_TAG, "no currency for code " + currencyId, exception);
                break;
        }
        return result;
    }

    /**
     *
     * @param weight 0, 1 или 2
     * @return
     */
    public static Spanned getRoubleSymbol(int weight) {
        String s;
        switch (weight) {
            case 0: s = "A"; break;
            case 1: s = "B"; break;
            default: s = "C"; break;
        }
        SpannableString string = new SpannableString(s);
        string.setSpan(new TypefaceSpan2(FontManager.getInstance().getRoubleFont()), 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return string;
    }

    /**
     * Какое-то мега ёбнутое форматирование
     */
    public static String formatNumber(Number in) {
        DecimalFormat myFormatter = new DecimalFormat("##,###,###.##");
        String split = myFormatter.format(in);
        return split.replace(",", ".");
    }

    //форматирование чисел
    public static String formatNumberNoFract(String in) {
        DecimalFormat myFormatter = new DecimalFormat("##,###,###");
        String split = myFormatter.format(Float.parseFloat(in));
        split = split.replace(",", ".");
        return split;
    }

    public static CharSequence formatUserId(String id) {
        if (id == null) return "";
        if (id.length() <= 3 || !id.matches("\\d+")) {
            return id;
        }
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        if (id.length() % 3 != 0) {
            pos = id.length() % 3;
            builder.append(id.substring(0, pos));
        } else {
            pos = 0;
        }

        int cnt = id.length() / 3;
        for (int i = 0; i < cnt; ++i) {
            if (builder.length() != 0) builder.append('\u2009');
            builder.append(id.substring(pos + i * 3, pos + 3 + i * 3));
        }

        return builder;
    }

    public static String dateFormat(Calendar calendar, Resources resources) {
        String dateOut = "";
        long diff = 0;
        int offSet = 0;
        int diffMin = 0;
        int diffHour = 0;

        Date currentDate = new Date();
        offSet = TimeZone.getDefault().getRawOffset() / 3600000;
        diff = (currentDate.getTime() - calendar.getTimeInMillis()) / 1000;
        diffMin = (int) diff / 60;
        diffHour = (int) diff / 3600;

        if (diff < 120) {
            dateOut = resources.getString(R.string.moment_ago);
        } else if (diff < 3600) {
            if ((diffMin == 11) | (diffMin == 12) | (diffMin == 13) | (diffMin == 14)) {
                dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago);
            } else if ((diffMin + "").endsWith("1")) {
                dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago2);
            } else if ((diffMin + "").endsWith("2") | (diffMin + "").endsWith("3") |
                    (diffMin + "").endsWith("4")) {
                dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago3);
            } else {
                dateOut = diffMin + " " +
                        resources.getString(R.string.min_ago);
            }
        } else if (diff < 7200) {
            dateOut = resources.getString(R.string.hour_ago);
        } else if (diff < 18000) {
            dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago1);
        } else if (diff < 75600) {
            dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago2);
        } else if ((diff > 75599) & (diff < 79200)) {
            dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago3);
        } else if (diff < 86400) {
            dateOut = diffHour + " " +
                    resources.getString(R.string.hour_ago1);
        } else {
            calendar.add(Calendar.HOUR_OF_DAY, offSet);
            String minute = calendar.get(Calendar.MINUTE) + "";
            if (minute.length() == 1) {
                minute = "0" + minute;
            }
            dateOut = calendar.get(Calendar.DAY_OF_MONTH) + " " +
                    resources.
                            getStringArray(R.array.month_array)[calendar.get(Calendar.MONTH)] +
                    ", " + calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                    minute;
        }
        return dateOut;
    }
}
