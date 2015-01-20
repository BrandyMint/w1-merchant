package com.w1.merchant.android.extra;

import android.content.Context;
import android.text.TextUtils;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.request.GETUserEntry;

public class UserEntrySupport {
	Context mCtx;

	public UserEntrySupport(Context context) {
		mCtx = context;
	}
	
	public void getData(String direction, int pageNumber, 
			String search, String token, String currency) {
		String[] requestData = {"", "", "", "", "", ""};
	    GETUserEntry getUserEntry;
	    
		//запрос списка операций
        requestData[0] = String.format(Constants.URL_USERENTRY, pageNumber, currency);
        if (!TextUtils.isEmpty(search)) {
        	requestData[0] += "&searchString=" + search;
        }
        if (direction.equals("Inc")) {
        	requestData[0] += "&direction=Inc"; 
		} else if (direction.equals("Out")) {
        	requestData[0] += "&direction=Out";
		}
        requestData[1] = token;
        getUserEntry = new GETUserEntry(mCtx);
        getUserEntry.execute(requestData);
		
    }

}
