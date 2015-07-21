package com.w1.merchant.android.utils.text;

import android.text.method.DigitsKeyListener;

import com.w1.merchant.android.utils.CurrencyHelper;

/**
 * Created by alexey on 21.07.15.
 */
public class CurrencyKeyListener extends DigitsKeyListener {

    private char[] mAccepted;

    public CurrencyKeyListener(String currencyId) {
        this(currencyId, false);
    }

    public CurrencyKeyListener(String currencyId, boolean digits) {
        super(false, digits);
        String accepted = "0123456789 \u202f" + CurrencyHelper.getCurrencySymbol(currencyId);
        if (digits) accepted += '.';
        mAccepted = new char[accepted.length()];
        accepted.getChars(0, accepted.length(), mAccepted, 0);
    }

    @Override
    protected char[] getAcceptedChars() {
        return mAccepted;
    }
}
