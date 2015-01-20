package ru.bokus.w1.Extra;

import android.content.Context;
import android.text.TextUtils;

import ru.bokus.w1.Activity.R;
import ru.bokus.w1.Request.GETUserEntry;

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
        requestData[0] = mCtx.getString(R.string.url_main) +
        		mCtx.getString(R.string.url_userentry, pageNumber + "", currency);
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
