package com.w1.merchant.android.viewextended;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.w1.merchant.android.utils.FontManager;

public class EditTextRouble extends EditText{

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
