package ru.bokus.w1.Extra;

import ru.bokus.w1.Request.GETUserEntry;
import ru.bokus.w1.Activity.R;
import android.content.Context;

public class OperationList {

	public static void get_Data(Context context, 
			String direction, int pageNumber, String token) {
		String[] requestData = { "", "" };
	    GETUserEntry getUserEntry;
	    
		//запрос списка операций
        requestData[0] = context.getString(R.string.url_main) +
        		context.getString(R.string.url_userentry, pageNumber + "");
        if (direction.equals("Inc")) {
        	requestData[0] += "&direction=Inc"; 
		} else if (direction.equals("Out")) {
        	requestData[0] += "&direction=Out";
		}
        requestData[1] = token;
        getUserEntry = new GETUserEntry(context);
        getUserEntry.execute(requestData);
		
    }
	
}
