package com.w1.merchant.android.extra;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.utils.Utils;

/**
 * Created by alexey on 22.02.15.
 */
public class LineChart extends com.github.mikephil.charting.charts.LineChart {
    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void prepareContentRect() {
        boolean changed = false;
        float offsetLeft = getOffsetLeft();
        float offsetRight = getOffsetRight();

        if (offsetLeft == Utils.convertDpToPixel(11f)) {
            changed = true;
            offsetLeft = 0;
        }

        if (offsetRight == Utils.convertDpToPixel(11f)) {
            changed = true;
            offsetRight = 0;
        }

        if (changed) {
            // Убираем странные константные 11dp по краям
            setOffsets(
                    Utils.convertPixelsToDp(offsetLeft),
                    Utils.convertPixelsToDp(getOffsetTop()),
                    Utils.convertPixelsToDp(offsetRight),
                    Utils.convertPixelsToDp(getOffsetBottom())
            );
        }

        super.prepareContentRect();
    }
}
