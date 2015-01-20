package com.w1.merchant.android.request;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.util.EntityUtils;

import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;
import android.os.AsyncTask;

public class HttpDELETE extends AsyncTask<String, Void, String[]> {
	
	public final Context mCtx;
	HttpClient httpclient;
	HttpDelete httpDelete;
	String[] result = { "", "" };
	HttpResponse response = null;
	HttpEntity httpEntity;
	
	public HttpDELETE(Context ctx) {
		mCtx = ctx;
	}
	
    @Override
    protected String[] doInBackground(String... data) {
    	httpDelete = new HttpDelete(data[0]);
	    try {
	        httpDelete.setHeader("Authorization", "Bearer " + data[1]);
	        httpDelete.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
	        response = httpclient.execute(httpDelete);
	        result[0] = response.getStatusLine().getStatusCode() + "";
	        httpEntity = response.getEntity();
	        result[1] = EntityUtils.toString(httpEntity, "UTF-8");
    	} catch (ClientProtocolException e) {
	    } catch (IOException e) {
	    }
		return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
    	super.onPostExecute(result);    
    }

    @Override
    protected void onPreExecute() {
    	super.onPreExecute();    
    	httpclient = LoginActivity.httpclient;
    }
    
    @Override
    protected void onCancelled() {
    	super.onCancelled();
    }
}
