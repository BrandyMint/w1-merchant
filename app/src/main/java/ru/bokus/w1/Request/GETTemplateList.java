package ru.bokus.w1.Request;

import java.util.ArrayList;

import ru.bokus.w1.Activity.MenuActivity;
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
