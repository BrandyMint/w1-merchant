package com.w1.merchant.android.utils;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.ui.widget.TypefaceSpan2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;

public final class TextUtilsW1 {

    private static final Spanned SPANNED_EMPTY = new SpannedString("");

    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
            "\\bhttp(?:s)?:\\/\\/\\S+?\\.(?:png|jpg|jpeg|gif|bmp|webp)\\b",
            Pattern.CASE_INSENSITIVE);

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
     * Замена ссылок на картинки в строке на Image span'ы.
     * Выполнять после Html.fromHtml, иначе с тегами возникнут проблемы.
     * @param source
     * @param imageGetter
     * @return
     */
    public static CharSequence replaceImgUrls(CharSequence source, Html.ImageGetter imageGetter) {
        SpannableStringBuilder str;
        int replacedUrls = 0;
        Deque<MatchResult> results = new ArrayDeque<>();

        if (source instanceof SpannableStringBuilder) {
            str = (SpannableStringBuilder)source;
        } else {
            str = new SpannableStringBuilder(source);
        }

        Matcher matcher =  IMAGE_URL_PATTERN.matcher(source);
        while (matcher.find()) {
            results.push(matcher.toMatchResult());
        }

        replacedUrls = results.size();
        while (!results.isEmpty()) {
            MatchResult matchResult = results.pop();
            String src = matchResult.group();
            if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "image url: " + src);
            ImageSpan span = new ImageSpan(imageGetter.getDrawable(src), src);

            str.replace(matchResult.start(), matchResult.end(), "\uFFFC");
            str.setSpan(span, matchResult.start(), matchResult.start() + "\uFFFC".length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (replacedUrls == 0) {
            return source;
        } else {
            return str;
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
            case "156": return "CNY";
            case "398": return "KZT";
            case "498": return "MDL";
            case "643": return "RUB";
            case "710": return "ZAR";
            case "840": return "USD";
            case "944": return "AZN";
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
            case "156":
                result = "¥";
                break;
            case "398":  //казах
                result = "₸";//KZTU+20B8&#8376
                break;
            case "498":
                result = "Leu";
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
            case "944":
                result = "man.";
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
                result = "Lari";//GEL U+20BE
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

    @Nullable
    @TargetApi(19)
    public static String getCurrencyName(String currencyId, Resources resources) {
        String result;
        int resId;
        Exception exception = null;
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                String iso4217Code = getIso4217CodeByNumber(currencyId);
                if (iso4217Code != null && !"RUB".equals(iso4217Code)) {
                    result = Currency.getInstance(iso4217Code).getDisplayName(Locale.getDefault());
                    if (!TextUtils.equals(result, iso4217Code)) return result;
                }
            } catch (Exception e) {
                exception = e;
            }
        }

        switch (currencyId) {
            case "156":
                resId = R.string.currency_CNY;
                break;
            case "398":  //казах
                resId = R.string.currency_KZT;
                break;
            case "498":
                resId = R.string.currency_MDL;
                break;
            case "643":  //рубль
                resId = R.string.currency_RUB;
                break;
            case "710": //южноафр
                resId = R.string.currency_ZAR;
                break;
            case "840": //USD
                resId = R.string.currency_USD;
                break;
            case "944":
                resId = R.string.currency_AZN;
                break;
            case "972": //таджик
                resId =R.string.currency_TJS;
                break;
            case "974": //белорус
                resId = R.string.currency_BYR;
                break;
            case "978": //EUR
                resId = R.string.currency_EUR;
                break;
            case "980": //укр
                resId = R.string.currency_UAH;
                break;
            case "981": //Груз.
                resId = R.string.currency_GEL;
                break;
            case "985": //польск
                resId = R.string.currency_PLN;
                break;
            default: //?
                if (BuildConfig.DEBUG)
                    Log.v(Constants.LOG_TAG, "no currency for code " + currencyId, exception);
                return null;
        }
        return resources.getString(resId);
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
     *
     * @return "100 $"
     */
    // XXX переделать всё на хуй. Должно быть не "100 $", а "$ 100". Использовать формат из системы,
    // а не свои велосипеды. Но при этом рубль должен быть новым символом, а не поддерживаемые
    // системой валюты - в нормальном виде. И всё в nowrap.
    public static CharSequence formatAmount(Number amount, String currencyId) {
        Spanned currencySymbol = getCurrencySymbol2(currencyId, 2);
        SpannableStringBuilder sb = new SpannableStringBuilder(formatNumber(amount));
        sb.append('\u00a0');
        sb.append(currencySymbol);
        return sb;
    }

    /**
     * Какое-то мега ёбнутое форматирование
     */
    public static String formatNumber(Number in) {
        DecimalFormat myFormatter = new DecimalFormat("##,###,###.##");
        String split = myFormatter.format(in);
        return split.replace(",", ".");
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

    public static CharSequence noWrap(CharSequence src) {
        if (TextUtils.indexOf(" ", src) < 0) return src;
        return TextUtils.replace(src, new String[]{" "}, new CharSequence[]{"\u00a0"});
    }

    // TODO Тесты
    @VisibleForTesting
    static int getCommissionTemplateResId(Provider.Commission commission) {
        if (commission.isZero()) {
            return R.string.commission_free;
        }

        if (!commission.hasPctRate()) {
            assertTrue(commission.hasAdditionalCost());
            return R.string.commission_cost;
        }

        boolean hasCost = commission.hasAdditionalCost();
        boolean hasMin = commission.hasMin();
        boolean hasMax = commission.hasMax();

        if (!hasCost) {
            if (hasMin) {
                return hasMax ? R.string.commission_rate_xx_from_yy_to_zz : R.string.commission_rate_xx_min_yy;
            } else {
                return hasMax ?  R.string.commission_rate_xx_max_yy : R.string.commission_rate_xx;
            }
        } else {
            if (hasMin) {
                return hasMax ? R.string.commission_rate_xx_cost_nn_min_yy_max_zz : R.string.commission_rate_xx_cost_nn_min_yy;
            } else {
                return hasMax ? R.string.commission_rate_xx_cost_nn_max_zz : R.string.commission_rate_xx_cost_nn;
            }
        }
    }

    public static CharSequence formatCommission(Provider.Commission commission, String currencyId, Resources resources) {
        CharSequence template = resources.getText(getCommissionTemplateResId(commission));

        String replaceSources[] = new String[] {
                "$rate", "$cost", "$from", "$plainfrom", "$to"
        };

        CharSequence replaceDestinations[] = new CharSequence[] {"", "", "", "", ""};
        if (commission.hasPctRate()) {
            NumberFormat pctFormat = DecimalFormat.getPercentInstance(Locale.getDefault());
            pctFormat.setMaximumFractionDigits(2);
            CharSequence rate = pctFormat.format(commission.rate.divide(BigDecimal.valueOf(100),
                    4, RoundingMode.UP));
            replaceDestinations[0] = noWrap(rate);
        }

        if (commission.hasAdditionalCost()) {
            replaceDestinations[1] = formatAmount(commission.cost, currencyId);
        }

        if (commission.hasMin()) {
            CharSequence plainFrom = formatNumber(commission.min);
            CharSequence from = formatAmount(commission.min, currencyId);
            replaceDestinations[2] = from;
            replaceDestinations[3] = plainFrom;
        }

        if (commission.hasMax()) {
            replaceDestinations[4] = formatAmount(commission.max, currencyId);
        }

        return TextUtils.replace(template, replaceSources, replaceDestinations);
    }

    public static CharSequence formatAmountRange(@Nullable Number from,
                                                 @Nullable Number to,
                                                 String currencyId,
                                                 Resources resources) {
        int templateId;

        if (from == null && to == null) return "";

        if (from != null && to != null) {
            templateId = R.string.amount_from_xx_to_yy;
        } else if (from != null) {
            templateId = R.string.amount_from_xx;
        } else {
            templateId = R.string.amount_to_yy;
        }

        String replaceSources[] = new String[] { "$from", "$plainfrom", "$to" };

        CharSequence replaceDestinations[] = new CharSequence[] {"", "", ""};
        if (from != null) {
            CharSequence plainFrom = formatNumber(from);
            CharSequence fromText = formatAmount(from, currencyId);
            replaceDestinations[0] = fromText;
            replaceDestinations[1] = plainFrom;
        }

        if (to != null) replaceDestinations[2] = formatAmount(to, currencyId);

        return TextUtils.replace(resources.getText(templateId), replaceSources, replaceDestinations);
    }


    @Nullable
    public static BigDecimal parseAmount(@Nullable CharSequence input) {
        if (TextUtils.isEmpty(input)) return null;
        StringBuilder stringBuilder = new StringBuilder(input.length());
        for (int i = 0, size = input.length(); i < size; i++) {
            char ch = input.charAt(i);
            if (Character.isDigit(ch)) {
                stringBuilder.append(ch);
            } else if(ch == ',' || ch == '.') {
                stringBuilder.append('.');
            }
        }

        try {
            return new BigDecimal(stringBuilder.toString());
        } catch (NumberFormatException ne) {
            // Ignore
        }
        return null;
    }
}
