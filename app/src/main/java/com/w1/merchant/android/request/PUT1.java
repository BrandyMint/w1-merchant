package com.w1.merchant.android.request;

import com.w1.merchant.android.activity.ConfirmOutActivity;
import android.content.Context;

public class PUT1 extends HttpPUT {

	public PUT1(Context ctx) {
		super(ctx);
	}
	
	@Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        //Log.d("1", line);
        if (!((ConfirmOutActivity) mCtx).isFinishing()) {
    		((ConfirmOutActivity) mCtx).putResult(result);
    	}
    }
}
