package ru.bokus.w1.Request;

import java.util.ArrayList;

import ru.bokus.w1.Activity.MenuActivity;
import android.content.Context;

public class GETUserEntryPeriod extends HttpGET {
	
	ArrayList<String[]> dataPeriod = null;

	public GETUserEntryPeriod(Context ctx) {
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
	        ((MenuActivity) mCtx).setDataGraphVPPercents(result);
        }
    }
}
