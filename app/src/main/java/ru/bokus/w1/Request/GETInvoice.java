package ru.bokus.w1.Request;

import java.util.ArrayList;
import java.util.Map;

import ru.bokus.w1.Activity.MenuActivity;
import android.content.Context;

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
	        data = JSONParsing.invoice(result[1], direction, currencyFilter);
	        ((MenuActivity) mCtx).addInvoice(data);
        }
    }
}
