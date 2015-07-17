package com.w1.merchant.android.ui.widget;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.w1.merchant.android.utils.FontManager;

//Расширение TextView для отображения шрифта со знаком рубля
// TODO: 17.07.15 избавиться
@Deprecated
public class TextViewRouble extends AppCompatTextView {

    private boolean initialized;

    public TextViewRouble(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public TextViewRouble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    public TextViewRouble(Context context) {
        super(context);
        onInit();
    }

    private void onInit() {
        if (initialized) return;
        initialized = true;
        if (!isInEditMode()) this.setTypeface(FontManager.getInstance().getRoubleFont());
    }
}
