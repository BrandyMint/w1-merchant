package ru.bokus.w1.Request;

import ru.bokus.w1.Activity.EditTemplate;
import android.content.Context;

public class GETTemplate extends HttpGET {

	public GETTemplate(Context ctx) {
		super(ctx);
	}

	@Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        //Log.d("1", line);
        if (!((EditTemplate) mCtx).isFinishing()) {
	        ((EditTemplate) mCtx).stopPBAnim();
	        ((EditTemplate) mCtx).httpResult(result[1]);
        }
    }
}
