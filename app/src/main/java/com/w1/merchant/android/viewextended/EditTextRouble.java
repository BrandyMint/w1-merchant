package com.w1.merchant.android.viewextended;

import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextRouble extends EditText{
	 
    public EditTextRouble(Context context) {
        super(context);
        this.setTypeface(LoginActivity.w1Rouble);
    }
 
    public EditTextRouble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(LoginActivity.w1Rouble);
    }
 
    public EditTextRouble(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(LoginActivity.w1Rouble);
    }
}
