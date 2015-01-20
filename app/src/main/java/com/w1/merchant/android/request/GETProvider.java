package com.w1.merchant.android.request;

import com.w1.merchant.android.activity.EditTemplate;
import android.content.Context;

public class GETProvider extends HttpGET {

	public GETProvider(Context ctx) {
		super(ctx);
	}

	@Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        //Log.d("1", line);
        if (!((EditTemplate) mCtx).isFinishing()) {
	        ((EditTemplate) mCtx).stopPBAnim();
	        ((EditTemplate) mCtx).providerResult(result[1]);
        }
    }
}
