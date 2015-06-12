package com.w1.merchant.android.ui.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by alexey on 31.07.14.
 */
public class DefaultUserpicDrawable extends Drawable {
    public static final int AVATAR_DIAMETER = 128;
    public static final int TEXT_SIZE = 42;
    private final Paint mBackgroundPaint;
    private final Paint mTextPaint;

    private int mBackgroundColor;
    private int mTextColor;
    private int mAlpha;
    private String mUsername;
    private Rect mBounds;

    private boolean mPaintsInvalidated;

    public DefaultUserpicDrawable(String username, int backgroundColor, int textColor) {
        mBackgroundPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mTextPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mAlpha = 255;
        setUser(username, backgroundColor, textColor);
    }

    public DefaultUserpicDrawable() {
        this("", 0, 0);
        setUser(null, Color.rgb(0x24, 0xb1, 0x66), Color.WHITE);
    }

    public void setUser(@Nullable String username) {
        String newUsername = TextUtils.isEmpty(username) ? "O" : username;
        if (!newUsername.equals(mUsername)) {
            mUsername = newUsername;
            invalidateSelf();
        }
    }

    public void setUser(@Nullable String username, int backgroundColor, int textColor) {
        mUsername = username == null ? "O" : username;
        mBackgroundColor = backgroundColor;
        mTextColor = textColor;
        mPaintsInvalidated = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mPaintsInvalidated) {
            invalidatePaints();
        }

        if (getOpacity() == 0) return;

        Rect bounds = getBounds();

        float diameter = Math.min(bounds.width(), bounds.height());

        canvas.drawCircle(bounds.centerX(), bounds.centerY(), diameter / 2.0f, mBackgroundPaint);

        Rect textBounds = new Rect();
        String text = String.valueOf(getUsernameFirstLettter());
        mTextPaint.getTextBounds(text, 0, 1, textBounds);

        canvas.drawText(String.valueOf(getUsernameFirstLettter()),
                bounds.centerX() + textBounds.left *0.4f,
                bounds.centerY() - textBounds.bottom + textBounds.height() / 2f,
                mTextPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            mBackgroundPaint.setAlpha(alpha);
            mTextPaint.setAlpha(mAlpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mBackgroundPaint != null) {
            mBackgroundPaint.setColorFilter(cf);
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        return mBackgroundPaint.getAlpha() < 255 ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
    }

    private char getUsernameFirstLettter() {
        if (TextUtils.isEmpty(mUsername)) return ' ';
        return Character.toUpperCase(mUsername.charAt(0));
    }

    private void invalidatePaints() {
        mBackgroundPaint.setColor(Color.argb(mAlpha, Color.red(mBackgroundColor), Color.green(mBackgroundColor), Color.blue(mBackgroundColor)));
        mBackgroundPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.argb(mAlpha, Color.red(mTextColor), Color.green(mTextColor), Color.blue(mTextColor)));
        //mTextPaint.setTypeface(FontManager.getInstance().getMainFont());
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        float textSize = TEXT_SIZE;
        mTextPaint.setTextSize(textSize);

        mPaintsInvalidated = true;
    }

    @Override
    public int getIntrinsicWidth() {
        return AVATAR_DIAMETER;
    }

    @Override
    public int getIntrinsicHeight() {
        return AVATAR_DIAMETER;
    }
}
