package com.w1.merchant.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by alexey on 14.06.15.
 */
public class HintedLinearLayout extends LinearLayout {
    private boolean initialized;
    private View.OnLongClickListener mOnLongClickListener;

    public static void showHint(View source, CharSequence contentDescription) {
        CharSequence contentDesc = source.getContentDescription();
        if (!TextUtils.isEmpty(contentDesc)) {
            int[] pos = new int[2];
            source.getLocationInWindow(pos);

            Resources r = source.getResources();
            int dy = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, r.getDisplayMetrics()) + 0.5f);

            Toast t = Toast.makeText(source.getContext(), contentDesc, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.LEFT, pos[0] - ((contentDesc.length() / 2) * 12), pos[1] - dy);
            t.show();
        }
    }

    public HintedLinearLayout(Context context) {
        super(context);
        init();
    }

    public HintedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HintedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HintedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    private final View.OnLongClickListener mOurOnLongClickListener = new View.OnLongClickListener() {

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
        showHint(this, getContentDescription());
    }

}
