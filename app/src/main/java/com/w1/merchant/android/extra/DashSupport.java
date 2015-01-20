package com.w1.merchant.android.extra;

import android.content.Context;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.request.GETUserEntryDash;
import com.w1.merchant.android.request.GETUserEntryPeriod;
import com.w1.merchant.android.request.GETUserEntryTotal;

public class DashSupport {
	Context mCtx;

	public DashSupport(Context context) {
		mCtx = context;
	}
	
	public void getData(int pageNumber, String token, String currency) {
		String[] requestData = { "", "", "", "" };
	    GETUserEntryDash getUserEntryDash;
	    
		//запрос списка операций
        requestData[0] = String.format(Constants.URL_USERENTRY, pageNumber, currency);
        requestData[1] = token;
        getUserEntryDash = new GETUserEntryDash(mCtx);
        getUserEntryDash.execute(requestData);
	}

	//для графика, процентов, ViewPager
	public void getDataPeriod(String createDate, String token, 
			String currency, String page) {
		String[] requestData2 = { "", "", "", "" };
	    GETUserEntryPeriod getUserEntryPeriod;
	    
		requestData2[0] = String.format(Constants.URL_USERENTRY_PERIOD, createDate, currency, page);
        requestData2[1] = token;
        getUserEntryPeriod = new GETUserEntryPeriod(mCtx);
        getUserEntryPeriod.execute(requestData2);
	}
	
	//итоги по выписке
	public void getDataTotal(String from, String to, 
			String direction, String token, String currency, String page) {
		String[] requestData3 = { "", "", "", "" };
	    GETUserEntryTotal getUserEntryTotal;
	    
		requestData3[0] = String.format(Constants.URL_USERENTRY_TOTAL, from, to, direction, currency, page);
        requestData3[1] = token;
        requestData3[2] = direction;
        getUserEntryTotal = new GETUserEntryTotal(mCtx);
        getUserEntryTotal.execute(requestData3);
	}
}
