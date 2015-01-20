package ru.bokus.w1.Extra;

import android.content.Context;

import ru.bokus.w1.Activity.R;
import ru.bokus.w1.Request.GETUserEntryDash;
import ru.bokus.w1.Request.GETUserEntryPeriod;
import ru.bokus.w1.Request.GETUserEntryTotal;

public class DashSupport {
	Context mCtx;

	public DashSupport(Context context) {
		mCtx = context;
	}
	
	public void getData(int pageNumber, String token, String currency) {
		String[] requestData = { "", "", "", "" };
	    GETUserEntryDash getUserEntryDash;
	    
		//запрос списка операций
        requestData[0] = mCtx.getString(R.string.url_main) +
        		mCtx.getString(R.string.url_userentry, pageNumber + "", currency);
        requestData[1] = token;
        getUserEntryDash = new GETUserEntryDash(mCtx);
        getUserEntryDash.execute(requestData);
	}

	//для графика, процентов, ViewPager
	public void getDataPeriod(String createDate, String token, 
			String currency, String page) {
		String[] requestData2 = { "", "", "", "" };
	    GETUserEntryPeriod getUserEntryPeriod;
	    
		requestData2[0] = mCtx.getString(R.string.url_main) +
        		mCtx.getString(R.string.url_userentry_period, createDate,
        				currency, page);
        requestData2[1] = token;
        getUserEntryPeriod = new GETUserEntryPeriod(mCtx);
        getUserEntryPeriod.execute(requestData2);
	}
	
	//итоги по выписке
	public void getDataTotal(String from, String to, 
			String direction, String token, String currency, String page) {
		String[] requestData3 = { "", "", "", "" };
	    GETUserEntryTotal getUserEntryTotal;
	    
		requestData3[0] = mCtx.getString(R.string.url_main) +
        		mCtx.getString(R.string.url_userentry_total,
        				from, to, direction, currency, page);
        requestData3[1] = token;
        requestData3[2] = direction;
        getUserEntryTotal = new GETUserEntryTotal(mCtx);
        getUserEntryTotal.execute(requestData3);
	}
}
