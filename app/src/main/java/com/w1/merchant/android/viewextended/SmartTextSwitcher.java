package com.w1.merchant.android.viewextended;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;

public class SmartTextSwitcher extends TextSwitcher {

    private boolean initialized;

    public SmartTextSwitcher(Context context) {
        super(context);
        initAnim();
    }

    public SmartTextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnim();
    }

    private void initAnim() {
        if (initialized) return;
        initialized = true;
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        in.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        out.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        setInAnimation(in);
        setOutAnimation(out);
    }

    @Nullable
    public CharSequence getText() {
        View currentView = getCurrentView();
        if (currentView != null) {
            return ((TextView)currentView).getText();
        } else {
            return null;
        }
    }

    public void setText(int resId) {
        setText(getResources().getText(resId));
    }

    public void setText(CharSequence text) {
        if (!TextUtils.equals(getText(), text)) {
            super.setText(text);
        }
    }
}
