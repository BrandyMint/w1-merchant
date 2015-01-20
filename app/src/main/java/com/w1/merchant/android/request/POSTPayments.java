package com.w1.merchant.android.request;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import com.w1.merchant.android.activity.ConfirmOutActivity;
import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;

public class POSTPayments extends HttpPOST {

	public POSTPayments(Context ctx) {
		super(ctx);
	}

	@Override
    protected String[] doInBackground(String... data) {
		httpclient = LoginActivity.httpclient;
		httppost = new HttpPost(data[0]);
	    try {
	    	httppost.setHeader("Authorization", "Bearer " + data[1]);
	        httppost.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
	        httppost.setHeader("Content-Type", "application/vnd.wallet.openapi.v1+json");
	        //Log.d("1", "Bearer " + data[1]);
	        jsonObj.put("TemplateId", data[2]);
	        httppost.setEntity(new StringEntity(jsonObj.toString(), HTTP.UTF_8));
	        response = httpclient.execute(httppost);
	        result[0] = response.getStatusLine().getStatusCode() + "";
	        httpEntity = response.getEntity();
	        result[1] = EntityUtils.toString(httpEntity, "UTF-8");
    	} catch (ClientProtocolException e) {
    		e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
    }
	
	@Override
    protected void onPostExecute(String[] result) {
    	super.onPostExecute(result);
    	if (!((ConfirmOutActivity) mCtx).isFinishing()) {
    		((ConfirmOutActivity) mCtx).paymentsResult(result);
    	}
    }
}
