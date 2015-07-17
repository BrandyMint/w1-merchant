package com.w1.merchant.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.w1.merchant.android.utils.FontManager;

public class TextViewRobotoThin extends AppCompatTextView {

    private boolean initialized;

    public TextViewRobotoThin(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public TextViewRobotoThin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    public TextViewRobotoThin(Context context) {
        super(context);
        onInit();
    }

    private void onInit() {
        if (initialized) return;
        initialized = true;
        if (!isInEditMode()) this.setTypeface(FontManager.getInstance().getThinFont());
    }
}
