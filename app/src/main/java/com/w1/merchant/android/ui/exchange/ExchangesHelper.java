package com.w1.merchant.android.ui.exchange;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.ExchangeRate;
import com.w1.merchant.android.rest.model.ExchangeRateStatus;
import com.w1.merchant.android.utils.CurrencyHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by alexey on 10.07.15.
 */
public final class ExchangesHelper {

    private static final int[] COLOR_SCHEMES = new int[] {
            R.color.scheme_green2, R.color.scheme_purple2, R.color.scheme_blue2, R.color.scheme_orange2,
            R.color.scheme_pink2, R.color.scheme_dove_colored2
    };

    private ExchangesHelper() {}


    static CharSequence getExchangeRateDescription(@Nullable ExchangeRate rate, Resources resources) {
        if (rate == null) return "";
        CharSequence template = resources.getText(R.string.exchange_rate_description);
        CharSequence commissionRate;
        NumberFormat commissionFormat = NumberFormat.getPercentInstance(Locale.getDefault());
        commissionFormat.setMaximumFractionDigits(2);
        commissionRate = commissionFormat.format(rate.commissionRate.divide(BigDecimal.valueOf(100), RoundingMode.DOWN));

        CharSequence amountFrom = CurrencyHelper.formatAmount(
                rate.rate.setScale(2, RoundingMode.UP),
                rate.srcCurrencyId);
        CharSequence amountTo = CurrencyHelper.formatAmount(BigDecimal.ONE, rate.dstCurrencyId);

        return TextUtils.replace(template,
                new String[]{"$srcCurrency", "$dstCurrency", "$comission"},
                new CharSequence[]{amountFrom, amountTo, commissionRate}
        );
    }

    @Nullable
    static Balance findBalance(Collection<Balance> balances, String currencyId) {
        for (Balance balance: balances) {
            if (currencyId.equals(balance.currencyId)) {
                return balance;
            }
        }
        return null;
    }

    static BigDecimal getBalanceAmount(Collection<Balance> balances, String currencyId) {
        Balance balance = findBalance(balances, currencyId);
        if (balance != null) {
            return balance.amount.setScale(0, RoundingMode.DOWN);
        } else {
            return BigDecimal.ZERO;
        }
    }

    static CharSequence getBalanceCardTitle(Collection<Balance> balances, String currencyId, Resources resources) {
        BigDecimal amount = getBalanceAmount(balances, currencyId).setScale(2, RoundingMode.DOWN);
        CharSequence currencyName = CurrencyHelper.getCurrencyName(currencyId, resources);
        CharSequence balanceText = CurrencyHelper.formatAmount(amount, currencyId);
        CharSequence template = resources.getText(R.string.exchange_currency_card_title);
        return TextUtils.replace(template, new String[] {"$currencyName", "$balance"},
                new CharSequence[] {currencyName, balanceText});
    }

    static boolean isCanBeExchanged(Balance balance, ExchangeRates rates) {
        return ((balance.isNative || balance.isVisible())
                && rates.containsSrcCurrency(balance.currencyId));
    }

    static CharSequence getExchangeRateStatusDescription(ExchangeRateStatus status, Resources resources) {
        CharSequence template = resources.getText(R.string.exchanged_xx_to_yy);
        return TextUtils.replace(template, new String[] {"$amountFrom", "$amountTo"},
                new CharSequence[] {
                        CurrencyHelper.formatAmount(status.srcAmount, status.srcCurrencyId),
                        CurrencyHelper.formatAmount(status.dstAmount, status.dstCurrencyId),
                });
    }

    public static boolean containsCurrencyCanBeExchanged(List<Balance> balances, ExchangeRates rates) {
        for (Balance balance: balances) {
            if (isCanBeExchanged(balance, rates)) return true;
        }
        return false;
    }

    static int getCurrencyBackgroundColor(Resources resources, String currencyId) {

        int colorResId;
        switch (currencyId) {
            case "643": colorResId = R.color.scheme_green; break;
            case "710": colorResId = R.color.scheme_purple; break;
            case "840": colorResId = R.color.scheme_blue; break;
            case "978": colorResId = R.color.scheme_orange; break;
            case "980": colorResId = R.color.scheme_pink; break;
            case "974": colorResId = R.color.scheme_dove_colored; break;
            default:
                colorResId = COLOR_SCHEMES[(currencyId.hashCode() % 7) % COLOR_SCHEMES.length];
        }
        return resources.getColor(colorResId);
    }
}
