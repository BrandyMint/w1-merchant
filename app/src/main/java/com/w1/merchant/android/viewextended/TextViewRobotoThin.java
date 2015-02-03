package com.w1.merchant.android.viewextended;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.w1.merchant.android.utils.FontManager;

//Расширение TextView для отображения шрифта со знаком рубля 
public class TextViewRobotoThin extends TextView {

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
        this.setTypeface(FontManager.getInstance().getThinFont());
    }
}
