package com.w1.merchant.android.request;

import com.w1.merchant.android.activity.MenuActivity;
import android.content.Context;

public class GETUserEntryTotal extends HttpGET {
	
	

	public GETUserEntryTotal(Context ctx) {
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
	        if (direction.equals("Inc")) {
	        	((MenuActivity) mCtx).userEntryInc(result);
	        } else if (direction.equals("Out")) {
	        	((MenuActivity) mCtx).userEntryOut(result);
	        }
        }
    }
}
