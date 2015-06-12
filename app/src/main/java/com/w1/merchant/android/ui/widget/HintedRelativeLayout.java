package com.w1.merchant.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by alexey on 12.06.15.
 */
public class HintedRelativeLayout extends RelativeLayout {
    private boolean initialized;
    private OnLongClickListener mOnLongClickListener;

    public HintedRelativeLayout(Context context) {
        super(context);
        init();
    }

    public HintedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HintedRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HintedRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        if (initialized) return;
        initialized = true;
        super.setOnLongClickListener(mOurOnLongClickListener);
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener l) {
        if (l == mOurOnLongClickListener) {
            super.setOnLongClickListener(l);
            return;
        }

        mOnLongClickListener = l;
    }

    private final OnLongClickListener mOurOnLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (mOnLongClickListener != null) {
                if (!mOnLongClickListener.onLongClick(v)) handleLongClick();
            } else {
                handleLongClick();
            }
            return true;
        }
    };

    private void handleLongClick() {
        HintedLinearLayout.showHint(this, getContentDescription());
    }

}
