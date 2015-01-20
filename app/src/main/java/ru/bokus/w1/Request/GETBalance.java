package ru.bokus.w1.Request;

import java.util.ArrayList;

import ru.bokus.w1.Activity.MenuActivity;
import android.content.Context;

public class GETBalance extends HttpGET {
	ArrayList<String[]> parseResult = new ArrayList<String[]>();
	
	public GETBalance(Context ctx) {
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
	        parseResult = JSONParsing.balance2(result[1]);
	        ((MenuActivity) mCtx).setBalance(parseResult);
        }
    }
}
