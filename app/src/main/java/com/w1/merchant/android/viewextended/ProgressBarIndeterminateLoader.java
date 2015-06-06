package com.w1.merchant.android.viewextended;

import android.content.Context;
import android.util.AttributeSet;

import com.w1.merchant.android.R;

/**
 * Created by alexey on 01.05.15.
 */
public class ProgressBarIndeterminateLoader extends android.widget.ProgressBar {

    private boolean mInitialized;

    public ProgressBarIndeterminateLoader(Context context) {
        super(context);
        init(context);
    }

    public ProgressBarIndeterminateLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressBarIndeterminateLoader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    void init(Context context) {
        if (mInitialized) return;
        mInitialized = true;
        setIndeterminateDrawable(new AnimatedRotateDrawable(context.getResources(), R.drawable.big_black_loader));

    }
}
