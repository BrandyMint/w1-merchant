package com.w1.merchant.android.request;

import android.content.Context;

import com.w1.merchant.android.activity.MenuActivity;

import java.util.ArrayList;
import java.util.Map;

public class GETInvoice extends HttpGET {
	
	ArrayList<Map<String, Object>> data = null;

	public GETInvoice(Context ctx) {
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
	        data = JSONParsing.invoice(result[1], direction, currencyFilter, mCtx.getResources());
	        ((MenuActivity) mCtx).addInvoice(data);
        }
    }
}
