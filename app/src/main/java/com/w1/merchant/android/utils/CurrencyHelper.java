package com.w1.merchant.android.utils;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by alexey on 19.07.15.
 */
public final class CurrencyHelper {

    public static final String ROUBLE_CURRENCY_NUMBER = "643";

    public static final String ROUBLE_SYMBOL = "\u20BD";

    private static final DecimalFormat sAmountFormatter;

    static {
        DecimalFormatSymbols formatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
        formatSymbols.setGroupingSeparator('\u202f');
        formatSymbols.setDecimalSeparator('.');
        sAmountFormatter = new DecimalFormat("##,###,###.##", formatSymbols);
        sAmountFormatter.setMaximumFractionDigits(2);
    }

    private CurrencyHelper() {
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
                result = "¥"; break;
            case "398":  //казах
                result = "₸";//KZTU+20B8&#8376
                break;
            case "498":
                result = "Leu";
                break;
            case "643":  //рубль
                result = "\u20BD";
                break;
            case "710": //южноафр
                result = "R";//ZARR
                break;
            case "840": //USD
                result = "$";//USD
                break;
            case "944":
                result = "man";
                break;
            case "972": //таджик
                result = "C";//TJSсмн.
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
                result = "\u20BE";//GEL U+20BE
                break;
            case "985": //польск
                result = "zł";//PLN
                break;
            default: //?
                result = currencyId;
                if (BuildConfig.DEBUG)
                    Log.v(Constants.LOG_TAG, "no currency for code " + currencyId, exception);
                break;
        }
        return result;
    }

    public static boolean isSignToLeft(String currencyId) {
        switch (currencyId) {
            case "710": //  South African rand
            case "840": // US Dollar
                return true;
            default:
                return false;
        }
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
     * @return "$ 100"
     */
    public static String formatAmount(Number amount, String currencyId) {
        synchronized (sAmountFormatter) {
            String amountText = sAmountFormatter.format(amount);
            if (isSignToLeft(currencyId)) {
                return getCurrencySymbol(currencyId) + '\u00a0' + amountText;
            } else {
                return amountText + '\u00a0' + getCurrencySymbol(currencyId);
            }
        }
    }

    @Nullable
    public static BigDecimal parseAmount(@Nullable CharSequence input) {
        if (TextUtils.isEmpty(input)) return null;
        StringBuilder stringBuilder = new StringBuilder(input.length());
        for (int i = 0, size = input.length(); i < size; i++) {
            char ch = input.charAt(i);
            if (TextUtilsW1.isAsciiDigit(ch)) {
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
