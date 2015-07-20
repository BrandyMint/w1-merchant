package com.w1.merchant.android.ui.widget;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

/**
 * Created by alexey on 20.07.15.
 */
public class PhoneEmailFormattingTextWatcher implements TextWatcher {

    /**
     * Indicates the change was caused by ourselves.
     */
    private boolean mSelfChange = false;

    /**
     * Indicates the formatting has been stopped.
     */
    private boolean mStopFormatting;

    private boolean mDoStopFormattingAfterTextChanged;

    private AsYouTypeFormatter mFormatter;

    private int mCursorDiff;

    public PhoneEmailFormattingTextWatcher() {
        mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mSelfChange || mStopFormatting || mDoStopFormattingAfterTextChanged) {
            return;
        }
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            mDoStopFormattingAfterTextChanged = true;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mSelfChange || mStopFormatting || mDoStopFormattingAfterTextChanged) {
            return;
        }
        // If the user inserted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            mDoStopFormattingAfterTextChanged = true;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mStopFormatting) {
            // Restart the formatting when all texts were clear.
            mStopFormatting = !(s.length() == 0);
            return;
        }

        if (mSelfChange) {
            // Ignore the change caused by s.replace().
            return;
        }

        if (mDoStopFormattingAfterTextChanged) {
            mDoStopFormattingAfterTextChanged = false;
            stopFormatting();
            mSelfChange = true;
            clearFormatting(s);
            mSelfChange = false;
            return;
        }


        String formatted = reformatPrepared(s, Selection.getSelectionEnd(s));
        if (formatted != null) {
            int rememberedPos = getRememberedPosition();
            mSelfChange = true;
            s.replace(0, s.length(), formatted, 0, formatted.length());
            // The text could be changed by other TextWatcher after we changed it. If we found the
            // text is not the one we were expecting, just give up calling setSelection().
            if (formatted.equals(s.toString())) {
                Selection.setSelection(s, rememberedPos);
            }
            mSelfChange = false;
        }
    }

    private String reformatPrepared(CharSequence s, int cursor) {
        if (s.length() < 2) {
            return null;
        }

        mCursorDiff = 0;

        if (s.charAt(0) == '+') {
            return reformat(s, cursor);
        }

        String input = s.toString();

        if (input.startsWith("89") || input.startsWith("8 9")) {
            // 89-. Считаем, что вводим российский медугородний номер
            String reformatted = reformat("+7" + input.substring(1), cursor + 1);
            if (reformatted != null) {
                if (reformatted.startsWith("+7")) {
                    mCursorDiff = -1;
                    return "8" + reformatted.substring(2);
                } else {
                    return reformatted;
                }
            }
        }

        // Ко всему остальному добавляем необязательный +
        String reformatted = reformat("+" + input, cursor + 1);
        if (reformatted == null) return null;

        if (reformatted.startsWith("+")) {
            mCursorDiff = -1;
            return reformatted.substring(1);
        } else {
            return reformatted;
        }
    }

    private int getRememberedPosition() {
        return mFormatter.getRememberedPosition() + mCursorDiff;
    }

    /**
     * Generate the formatted number by ignoring all non-dialable chars and stick the cursor to the
     * nearest dialable char to the left. For instance, if the number is  (650) 123-45678 and '4' is
     * removed then the cursor should be behind '3' instead of '-'.
     */
    private String reformat(CharSequence s, int cursor) {
        // The index of char to the leftward of the cursor.
        int curIndex = cursor - 1;
        String formatted = null;
        mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        return hasCursor ? mFormatter.inputDigitAndRememberPosition(lastNonSeparator)
                : mFormatter.inputDigit(lastNonSeparator);
    }

    private void stopFormatting() {
        mStopFormatting = true;
        mFormatter.clear();
    }

    private void clearFormatting(Editable s) {
        if (TextUtils.isEmpty(s)) return;
        int cursor = Selection.getSelectionEnd(s);

        // XXX не учитываем ситуацию, когда пробел вставлен в середину строки
        StringBuilder cleaned = new StringBuilder(s.length());
        int length = s.length();

        for (int i = 0; i < length - 1; i++) {
            switch (s.charAt(i)) {
                case ' ':
                case '-':
                case '(':
                case ')':
                    if (cursor >= i) cursor -= 1;
                    break;
                default:
                    cleaned.append(s.charAt(i));
                    break;
            }
        }
        cleaned.append(s.charAt(length - 1));
        s.replace(0, s.length(), cleaned, 0, cleaned.length());
        // The text could be changed by other TextWatcher after we changed it. If we found the
        // text is not the one we were expecting, just give up calling setSelection().
        if (cleaned.toString().equals(s.toString()) && cursor >= 0) {
            Selection.setSelection(s, cursor);
        }
    }

    private boolean hasSeparator(final CharSequence s, final int start, final int count) {
        for (int i = start; i < start + count; i++) {
            char c = s.charAt(i);
            if (!PhoneNumberUtils.isNonSeparator(c)) {
                return true;
            }
        }
        return false;
    }
}
