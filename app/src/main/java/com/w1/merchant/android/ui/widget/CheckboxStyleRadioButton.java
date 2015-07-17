package com.w1.merchant.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by alexey on 27.02.15.
 */
public class CheckboxStyleRadioButton extends AppCompatRadioButton {
    public CheckboxStyleRadioButton(Context context) {
        super(context);
    }

    public CheckboxStyleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckboxStyleRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
