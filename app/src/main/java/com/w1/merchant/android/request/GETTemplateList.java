package com.w1.merchant.android.request;

import java.util.ArrayList;

import com.w1.merchant.android.activity.MenuActivity;
import android.content.Context;

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
	        data = JSONParsing.template(result[1]);
	        ((MenuActivity) mCtx).addTemplateList(data);
        }
    }
}
