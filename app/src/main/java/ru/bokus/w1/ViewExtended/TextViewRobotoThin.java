package ru.bokus.w1.ViewExtended;

import ru.bokus.w1.Activity.LoginActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

//Расширение TextView для отображения шрифта со знаком рубля 
public class TextViewRobotoThin extends TextView {

    public TextViewRobotoThin(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(LoginActivity.tfRobotoThin);
    }

    public TextViewRobotoThin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTypeface(LoginActivity.tfRobotoThin);
    }

    public TextViewRobotoThin(Context context) {
        super(context);
        this.setTypeface(LoginActivity.tfRobotoThin);
    }

}
