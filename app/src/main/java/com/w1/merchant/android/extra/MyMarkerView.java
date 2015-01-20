
package com.w1.merchant.android.extra;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MarkerView;
import com.github.mikephil.charting.utils.Utils;
import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.MenuActivity;
import com.w1.merchant.android.request.JSONParsing;

public class MyMarkerView extends MarkerView {

    private TextView tvContent, tvDate;
    MenuActivity menuActivity;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        menuActivity = (MenuActivity) context;
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvDate = (TextView) findViewById(R.id.tvDate);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content
    @Override
    public void refreshContent(Entry e, int dataSetIndex) {
    	
        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
        } else {
        	// define an offset to change the original position of the marker
            // (optional)
        	
        	setOffsets(-getMeasuredWidth() / 2, -getMeasuredHeight() / 2);
        	int[] location = new int[2];
        	getLocationInWindow(location);
        	String amount = e.getVal() + "";
	        tvContent.setText(JSONParsing.formatNumberNoFract(amount));
            if (menuActivity.currentPlot == menuActivity.PLOT_24) {
            	if (menuActivity.dataPlotDayX.size() > 0) {
            		tvDate.setText(menuActivity.dataPlotDayX.get(e.getXIndex()));
            	}	
			} else if (menuActivity.currentPlot == menuActivity.PLOT_WEEK) {
				if (menuActivity.dataPlotWeekX.size() > 0) {
					tvDate.setText(menuActivity.dataPlotWeekX.get(e.getXIndex()));
				}
			} else if (menuActivity.currentPlot == menuActivity.PLOT_30) {
				if (menuActivity.dataPlotMonthX.size() > 0) {
					tvDate.setText(menuActivity.dataPlotMonthX.get(e.getXIndex()));
				}
			}
        }
    }
}
