package com.w1.merchant.android.viewextended;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;

public class ImageLoadingGetter implements Html.ImageGetter {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private final Drawable mPlaceholderDrawable;

    private int mDstWidth;

    /**
     * {@linkto Html.ImageGetter} - placeholder для картинок, которые тегами img в тексте.
     * @param width Ширина placeholder'а. Может быть 0
     * @param context
     */
    public ImageLoadingGetter(int width, Context context) {
        if (width <= 0) {
            mDstWidth = context.getResources().getDimensionPixelSize(R.dimen.image_loading_placeholder_default_size);
        } else {
            mDstWidth = width;
        }

        int height = Math.round(mDstWidth / (4f / 3f));
        mPlaceholderDrawable = context.getResources().getDrawable(R.drawable.image_loading_drawable).mutate();
        mPlaceholderDrawable.setBounds(0, 0, mDstWidth, height);
    }

    public void setWidth(int width) {
        if (width <= 0) return;
        int height = Math.round(mDstWidth / (4f / 3f));
        mDstWidth = width;
        mPlaceholderDrawable.setBounds(0, 0, mDstWidth, height);
    }

    public int getWidth() {
        return mDstWidth;
    }

    @Override
    public Drawable getDrawable(String source) {
        if (DBG) Log.v(TAG, "getDrawable() " + source);
        return mPlaceholderDrawable;
    }
}
