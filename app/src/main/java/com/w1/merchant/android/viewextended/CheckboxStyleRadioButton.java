package com.w1.merchant.android.viewextended;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by alexey on 27.02.15.
 */
public class CheckboxStyleRadioButton extends RadioButton {
    public CheckboxStyleRadioButton(Context context) {
        super(context);
    }

    public CheckboxStyleRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckboxStyleRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckboxStyleRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
