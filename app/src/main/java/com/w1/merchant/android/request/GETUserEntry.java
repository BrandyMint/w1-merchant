package com.w1.merchant.android.request;

import java.util.ArrayList;
import java.util.Map;

import com.w1.merchant.android.activity.MenuActivity;
import android.content.Context;


public class GETUserEntry extends HttpGET {
	
	ArrayList<Map<String, Object>> data = null;

	public GETUserEntry(Context ctx) {
		super(ctx);
	}
	
	@Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	((MenuActivity) mCtx).startPBAnim();
    }

	@Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if (!((MenuActivity) mCtx).isFinishing()) {
	        ((MenuActivity) mCtx).stopPBAnim();
	        
	        ((MenuActivity) mCtx).addUserEntry(result[1]);
        }
    }
}