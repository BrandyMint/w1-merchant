package ru.bokus.w1.Request;

import ru.bokus.w1.Activity.MenuActivity;
import android.content.Context;

public class GETProfile extends HttpGET {
	String[] parseResult = { "", "", "" };
	
	public GETProfile(Context ctx) {
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
	        parseResult = JSONParsing.profile(result[1]);
	        ((MenuActivity) mCtx).setProfile(parseResult);
        }
    }
}
