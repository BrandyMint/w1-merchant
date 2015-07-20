package com.w1.merchant.android.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Patterns;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
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

    private static final Pattern CLEANUP_NUMBER_PATTERN = Pattern.compile("(?:^\\s+)|(?:[ \\(\\)\\-]+)|(?:\\s+$)");

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

    public static boolean isPossibleEmail(CharSequence text) {
        if (TextUtils.isEmpty(text)) return false;
        return Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    /**
     * Очистка строки от символов форматирования телефонного номера.
     * Удаляются пробелы и символы <code>[-+()]</code>
     * @param possibleNumber
     * @return
     */
    @VisibleForTesting
    static String cleanupPhoneNumber(CharSequence possibleNumber) {
        return CLEANUP_NUMBER_PATTERN.matcher(possibleNumber).replaceAll("");
    }

    /**
     * Мы принимаем номера только в международном формате. Но делаем исключение для российского
     * междугороднего формата, мобильных телефонов (89-). Считаем, что + в начале ставить не обязательно.
     *
     * @param input Строка из формы ввода
     * @return input с 89 в начале замененым на +7, все остальнео - если пюса нет, то с дего добавлением
     */
    public static String preparePhoneInternationalFormat(String input) {
        String maybePhone = null;
        if (TextUtils.isEmpty(input)) return input;

        if (input.length() <= 3) return input;

        if (input.startsWith("+")) {
            maybePhone = input;
        } else if (input.startsWith("89")) {
            maybePhone = "+7" + input.substring(1);
        } else {
            maybePhone = "+" + input;
        }
        return maybePhone;
    }

    /**
     * Проверка на валидность номер телефона.
     */
    public static boolean isValidPhoneNumber(CharSequence text) {
        String maybePhone;
        if (TextUtils.isEmpty(text)) return false;

        String textCleaned = cleanupPhoneNumber(text);
        maybePhone = preparePhoneInternationalFormat(textCleaned);

        try {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = util.parse(maybePhone, null);
            if (BuildConfig.DEBUG) Log.v("TextUtilsW1", "number: " + number.toString());
            return util.isValidNumber(number);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return false;
        //return PHONE_NUMBER_PATTERN.matcher(text).matches();
    }

    /**
     * Упрощенная проверка на валидность номера
     */
    public static boolean isPossiblePhoneNumber(CharSequence input) {
        if (TextUtils.isEmpty(input)) return false;
        String textCleaned = cleanupPhoneNumber(input);
        String maybePhone = preparePhoneInternationalFormat(textCleaned);
        return PhoneNumberUtil.getInstance().isPossibleNumber(maybePhone, null);
    }

    /**
     * Парсинг телефонного номера с учетом того, что мы принимаем номера только в международном
     * формате, но делаем исключение для российских мобильных в междугороднем формате
     * @param input
     * @return
     */
    @Nullable
    public static Phonenumber.PhoneNumber parsePhoneNumber(CharSequence input) {
        if (TextUtils.isEmpty(input)) return null;
        String phoneInternationalFormat = preparePhoneInternationalFormat(cleanupPhoneNumber(input));
        try {
            return PhoneNumberUtil.getInstance().parse(phoneInternationalFormat, null);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
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
            replaceDestinations[1] = CurrencyHelper.formatAmount(commission.cost, currencyId);
        }

        if (commission.hasMin()) {
            CharSequence plainFrom = formatNumber(commission.min);
            CharSequence from = CurrencyHelper.formatAmount(commission.min, currencyId);
            replaceDestinations[2] = from;
            replaceDestinations[3] = plainFrom;
        }

        if (commission.hasMax()) {
            replaceDestinations[4] = CurrencyHelper.formatAmount(commission.max, currencyId);
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
            CharSequence fromText = CurrencyHelper.formatAmount(from, currencyId);
            replaceDestinations[0] = fromText;
            replaceDestinations[1] = plainFrom;
        }

        if (to != null) replaceDestinations[2] = CurrencyHelper.formatAmount(to, currencyId);

        return TextUtils.replace(resources.getText(templateId), replaceSources, replaceDestinations);
    }


}
