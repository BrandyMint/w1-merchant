package com.w1.merchant.android.utils.text;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by alexey on 22.07.15.
 */
public class TextWatcherWrapper implements TextWatcher {

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
