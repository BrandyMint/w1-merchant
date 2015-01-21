package com.w1.merchant.android.request;

import android.content.Context;

import com.w1.merchant.android.activity.MenuActivity;

import java.util.ArrayList;

public class GETTemplateList extends HttpGET {
	
	ArrayList<String[]> data = null;

	public GETTemplateList(Context ctx) {
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
	        data = JSONParsing.template(result[1], mCtx.getResources());
	        ((MenuActivity) mCtx).addTemplateList(data);
        }
    }
}
