package com.w1.merchant.android.extra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.renderer.ViewPortHandler;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.w1.merchant.android.R;
import com.w1.merchant.android.utils.TextUtilsW1;

/**
 * Created by alexey on 22.02.15.
 */
public class LineChart extends com.github.mikephil.charting.charts.LineChart {

    private boolean mInitialized = false;

    public LineChart(Context context) {
        super(context);
        onInit();
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    @Override
    protected void init() {
        super.init();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mRenderer = new MyLineChartRenderer(this, mAnimator, mViewPortHandler);

        mLeftAxisTransformer = new HackyTransformer(mViewPortHandler);
        mRightAxisTransformer = new HackyTransformer(mViewPortHandler);

        mAxisRendererLeft = new YAxisRenderer(mViewPortHandler, mAxisLeft, mLeftAxisTransformer);
        mAxisRendererRight = new YAxisRenderer(mViewPortHandler, mAxisRight, mRightAxisTransformer);

        mXAxisRenderer = new XAxisRenderer(mViewPortHandler, mXAxis, mLeftAxisTransformer);
    }

    private void onInit() {
        if (mInitialized) return;
        mInitialized = true;

        setDescription("");
        setNoDataTextDescription("");
        setNoDataText("");
        setHighlightEnabled(true);
        setTouchEnabled(true);
        setDoubleTapToZoomEnabled(false);
        setDragEnabled(false);
        setScaleEnabled(false);
        setDrawGridBackground(false);
        setPinchZoom(false);
        getXAxis().setEnabled(false);
        getAxisLeft().setEnabled(false);
        getAxisRight().setEnabled(false);
        setViewPortOffsets(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.graphic_bottom_offset));

        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        setMarkerView(mv);

        LimitLine ll = new LimitLine(0f);
        ll.setLineColor(Color.parseColor("#434343"));
        ll.setLineWidth(1.5f);
        ll.enableDashedLine(5f, 10f, 0);
        getAxisLeft().addLimitLine(ll);
        getAxisLeft().setStartAtZero(true);
        getAxisRight().setStartAtZero(true);

    }

    @Override
    public void setData(LineData data) {
        super.setData(data);
        if (getLegend() != null) getLegend().setEnabled(false);
    }

    private static class MyLineChartRenderer extends LineChartRenderer {

        public MyLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
        }

        @Override
        public void drawHighlighted(Canvas c, Highlight[] indices) {

            for (int i = 0; i < indices.length; i++) {

                LineDataSet set = mChart.getLineData().getDataSetByIndex(indices[i]
                        .getDataSetIndex());

                if (set == null)
                    continue;

                mHighlightPaint.setColor(set.getHighLightColor());

                int xIndex = indices[i].getXIndex(); // get the
                // x-position

                if (xIndex > mChart.getXChartMax() * mAnimator.getPhaseX())
                    continue;

                float y = set.getYValForXIndex(xIndex) * mAnimator.getPhaseY(); // get
                // the
                // y-position

                float[] pts = new float[]{
                        xIndex, mChart.getYChartMax(), xIndex, mChart.getYChartMin(), 0, y,
                        mChart.getXChartMax(), y
                };

                mChart.getTransformer(set.getAxisDependency()).pointValuesToPixel(pts);
                // draw the highlight lines
                c.drawLines(pts, mHighlightPaint);
            }
        }

    }

    private static class MyMarkerView extends MarkerView {

        private TextView tvContent, tvDate;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = (TextView) findViewById(R.id.tvContent);
            tvDate = (TextView) findViewById(R.id.tvDate);
        }

        @Override
        public void draw(Canvas canvas, float posx, float posy) {
            if (posx + getXOffset() < 0) {
                posx = -getXOffset();
            } else if (posx + getXOffset() + getWidth() > canvas.getWidth()) {
                posx = canvas.getWidth() - getWidth() - getXOffset();
            }
            super.draw(canvas, posx, posy);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content
        @Override
        public void refreshContent(Entry e, int dataSetIndex) {
            if (e instanceof CandleEntry) {
                CandleEntry ce = (CandleEntry) e;
                tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
            } else {
                tvDate.setText((CharSequence) e.getData());
                tvContent.setText(TextUtilsW1.formatNumber(Math.round(e.getVal())));
            }
        }

        @Override
        public int getXOffset() {
            return -(getWidth() / 2);
        }

        @Override
        public int getYOffset() {
            return -(getHeight() / 2);
        }
    }

    public static class HackyTransformer extends Transformer {

        private final ViewPortHandler mViewPortHandler;

        public HackyTransformer(ViewPortHandler viewPortHandler) {
            super(viewPortHandler);
            mViewPortHandler = viewPortHandler;
        }

        @Override
        public void prepareMatrixValuePx(float xChartMin, float deltaX, float deltaY, float yChartMin) {
            if (deltaX == 0f) deltaX = mViewPortHandler.getChartHeight();
            if (deltaY == 0f) deltaY = mViewPortHandler.getChartWidth();
            super.prepareMatrixValuePx(xChartMin, deltaX, deltaY, yChartMin);
        }

        @Override
        public void prepareMatrixOffset(boolean inverted) {
            mMatrixOffset.reset();

            if (!inverted) {
                float bottom = mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom();
                bottom -= Utils.convertDpToPixel(8);

                mMatrixOffset.postTranslate(mViewPortHandler.offsetLeft(),
                        bottom);
            } else {
                super.prepareMatrixOffset(inverted);
            }
        }
    }
}
