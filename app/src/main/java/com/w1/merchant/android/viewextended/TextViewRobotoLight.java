package com.w1.merchant.android.viewextended;

import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

//Расширение TextView для отображения шрифта со знаком рубля 
public class TextViewRobotoLight extends TextView {

    public TextViewRobotoLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(LoginActivity.tfRobotoLight);
    }

    public TextViewRobotoLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(LoginActivity.tfRobotoLight);
    }

    public TextViewRobotoLight(Context context) {
        super(context);
        this.setTypeface(LoginActivity.tfRobotoLight);
    }

}
