package ru.bokus.w1.ViewExtended;

import ru.bokus.w1.Activity.LoginActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

//Расширение TextView для отображения шрифта со знаком рубля 
public class TextViewRouble extends TextView {

    public TextViewRouble(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(LoginActivity.w1Rouble);
    }

    public TextViewRouble(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(LoginActivity.w1Rouble);
    }

    public TextViewRouble(Context context) {
        super(context);
        this.setTypeface(LoginActivity.w1Rouble);
    }

}
