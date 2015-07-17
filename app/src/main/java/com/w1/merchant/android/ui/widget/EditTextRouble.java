package com.w1.merchant.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

import com.w1.merchant.android.utils.FontManager;

public class EditTextRouble extends AppCompatEditText {

    private boolean initialized;

    public EditTextRouble(Context context) {
        super(context);
        onInit();
    }
 
    public EditTextRouble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }
 
    public EditTextRouble(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    private void onInit() {
        if (initialized) return;
        initialized = true;
        if (!isInEditMode()) this.setTypeface(FontManager.getInstance().getRoubleFont());
    }
}
