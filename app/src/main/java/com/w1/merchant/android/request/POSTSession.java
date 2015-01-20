package com.w1.merchant.android.request;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;
import android.view.View;

public class POSTSession extends HttpPOST {

	public POSTSession(Context ctx) {
		super(ctx);
	}

	@Override
    protected String[] doInBackground(String... data) {
		httpclient = LoginActivity.httpclient;
    	httppost = new HttpPost(data[0]);
	    try {
	    	jsonObj.put("Login", data[1]);
			jsonObj.put("Password", data[2]);
		    jsonObj.put("Scope", "All");
			httppost.setHeader("Authorization", "Bearer {54344285-82DA-42EA-B7D0-0C9B978FFD89}");
	        httppost.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
	        httppost.setHeader("Content-Type", "application/vnd.wallet.openapi.v1+json");
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
    protected void onPreExecute() {
   		super.onPreExecute();
   		((LoginActivity) mCtx).pbLogin.setVisibility(View.VISIBLE);
    }
	
	@Override
    protected void onPostExecute(String[] result) {
    	super.onPostExecute(result);
    	if (!((LoginActivity) mCtx).isFinishing()) {
	    	((LoginActivity) mCtx).pbLogin.setVisibility(View.INVISIBLE);
	    	((LoginActivity) mCtx).sessionResult(result);
    	}
    }
}
