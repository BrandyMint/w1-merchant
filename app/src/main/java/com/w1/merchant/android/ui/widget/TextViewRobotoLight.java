package com.w1.merchant.android.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.w1.merchant.android.utils.FontManager;


public class TextViewRobotoLight extends AppCompatTextView {

    private boolean initialized;

    public TextViewRobotoLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public TextViewRobotoLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    public TextViewRobotoLight(Context context) {
        super(context);
        onInit();
    }

    private void onInit() {
        if (initialized) return;
        initialized = true;
        if (!isInEditMode()) this.setTypeface(FontManager.getInstance().getLightFont());
    }
}
