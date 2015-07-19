package com.w1.merchant.android.ui.exchange;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    static class TextWatcherWrapper implements TextWatcher {

        private boolean mEditing;

        private boolean mEnable = true;

        private final OnTextChangedListener mListener;

        public TextWatcherWrapper(OnTextChangedListener listener) {
            this.mListener = listener;
            mEnable = true;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Ignore
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Ignore
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEditing || !mEnable) return;
            mEditing = true;
            try {
                mListener.onTextChanged(s);
            } finally {
                mEditing = false;
            }
        }

        public void disable() {
            mEnable = false;
        }

        public void enable() {
            mEnable = true;
        }

        public interface OnTextChangedListener {
            void onTextChanged(Editable s);
        }
    }

    public static class CurrencyFormatInputFilter implements InputFilter {

        private static Pattern sPattern = Pattern.compile("(?:0|[1-9]+[0-9]*)(?:\\.[0-9]{0,2})?");

        private Matcher mMatcher = sPattern.matcher("");

        private StringBuilder mStringBuilder = new StringBuilder();

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (start == end) return null;
            String source2 = source.toString().substring(start, end);
            if (".".equals(source2) && dstart == 0 && dest.length() == 0) {
                if (source instanceof  Spanned) {
                    SpannableString sp = new SpannableString("0" + source2);
                    TextUtils.copySpansFrom((Spanned)source, start, end, null, sp, 1);
                    return sp;
                } else {
                    return "0.";
                }
            }
            mStringBuilder.append(dest);
            mStringBuilder.replace(dstart, dend, source2);
            mMatcher.reset(mStringBuilder);
            boolean matches = mMatcher.matches();
            mStringBuilder.setLength(0);
            return matches ? null : dest.subSequence(dstart, dend);
        }
    }

}
