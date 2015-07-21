package com.w1.merchant.android.utils.text;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by alexey on 21.07.15.
 */
public class CurrencyFormattingTextWatcher implements TextWatcher {

    private final String mCurrencyId;

    private final DecimalFormat mAmountFormat;

    private boolean mSelfChange;

    // TODO
    private boolean mFractionDigits;

    private int mRememberedCursor = -1;

    public CurrencyFormattingTextWatcher(String currencyId) {
        this(currencyId, false);
    }

    public CurrencyFormattingTextWatcher(String currencyId, boolean inputFractions) {
        mCurrencyId = currencyId;
        mFractionDigits = inputFractions;
        DecimalFormatSymbols formatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
        formatSymbols.setGroupingSeparator('\u202f');
        formatSymbols.setDecimalSeparator('.');
        mAmountFormat = new DecimalFormat("##,###,###.##", formatSymbols);
        mAmountFormat.setMaximumFractionDigits(mFractionDigits ? 2 : 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSelfChange) return;

        String formatted = reformat(s);
        if (!TextUtils.equals(s, formatted)) {
            mSelfChange = true;
            s.replace(0, s.length(), formatted);
            if (formatted.equals(s.toString())) {
                if (mRememberedCursor > 0) Selection.setSelection(s, mRememberedCursor);
            }
            mSelfChange = false;
        }
    }

    private String reformat(Editable input) {
        mRememberedCursor = -1;
        int cursor = Selection.getSelectionEnd(input);
        int decimalSeparatorPosition = -1;
        StringBuilder stringBuilder = new StringBuilder(input.length());

        //Clean
        for (int i = 0, length = input.length(); i < length; ++i) {
            char ch = input.charAt(i);
            if (TextUtilsW1.isAsciiDigit(ch)) {
                stringBuilder.append(ch);
            } else if((ch == ',' || ch == '.')
                    && mFractionDigits
                    && (decimalSeparatorPosition < 0)) {
                decimalSeparatorPosition = i;
                stringBuilder.append('.');
            } else {
                if (cursor > i) cursor -= 1;
            }
        }

        if (cursor > stringBuilder.length()) cursor = stringBuilder.length();
        if (stringBuilder.length() == 0) {
            mRememberedCursor = cursor;
            return addCurrencyIndicator("");
        }

        if (mFractionDigits) {
            if (TextUtils.equals(".", stringBuilder)
                    //|| TextUtils.equals("0", stringBuilder)
                    ) {
                mRememberedCursor = cursor < 0 ? -1 : 2;
                return addCurrencyIndicator("0.");
            } else if (stringBuilder.charAt(stringBuilder.length() - 1) == '.') {
                // XXX Здесь исчезает форматирования целой части
                mRememberedCursor = cursor;
                return addCurrencyIndicator(stringBuilder.toString());
            }
        } else {
            if (TextUtils.equals("0", stringBuilder)) {
                mRememberedCursor = cursor < 0 ? -1 : 0;
                return addCurrencyIndicator("");
            }
        }

        try {
            BigDecimal value =  new BigDecimal(stringBuilder.toString());
            if (value.scale() > 2) value = value.setScale(2, RoundingMode.DOWN);
            mAmountFormat.setMinimumFractionDigits(value.scale());

            String amount = mAmountFormat.format(value);
            cursor += countFormattingCharacters(amount, cursor);
            if (cursor > amount.length()) cursor = amount.length();
            mRememberedCursor = cursor;
            return addCurrencyIndicator(amount);
        } catch (NumberFormatException ne) {
            return addCurrencyIndicator("");
        }
    }

    private String addCurrencyIndicator(String string) {
        String currencyPrefix = getCurrencyPrefix();
        if (string.isEmpty()) {
            return string;
        } else {
            if (mRememberedCursor >= 0) mRememberedCursor += currencyPrefix.length();
            return currencyPrefix + string + getCurrencySuffix();
        }
    }

    private boolean isFormattingCharacter(char codePoint) {
        return codePoint == ' '
                || codePoint == '\u00a0'
                || codePoint == '\u2009'
                || codePoint == '\u202f'
                ;
    }

    private String getCurrencySuffix() {
        if (!CurrencyHelper.isSignToLeft(mCurrencyId)) {
            return " " + CurrencyHelper.getCurrencySymbol(mCurrencyId);
        } else {
            return "";
        }
    }

    private String getCurrencyPrefix() {
        if (CurrencyHelper.isSignToLeft(mCurrencyId)) {
            return CurrencyHelper.getCurrencySymbol(mCurrencyId) + " ";
        } else {
            return "";
        }
    }

    private int countFormattingCharacters(String test, int toCursor) {
        if (toCursor <= 0) return 0;
        int cursor = 0;
        int formattingCharacters = 0;
        for (int i = 0, length = test.length(); i < length; i++) {
            if (isFormattingCharacter(test.charAt(i))) {
                formattingCharacters += 1;
            } else {
                cursor += 1;
                if (cursor == toCursor) break;
            }
        }
        return formattingCharacters;
    }
}
