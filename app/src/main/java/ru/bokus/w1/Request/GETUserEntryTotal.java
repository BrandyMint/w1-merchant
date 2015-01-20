package ru.bokus.w1.Request;

import ru.bokus.w1.Activity.MenuActivity;
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
