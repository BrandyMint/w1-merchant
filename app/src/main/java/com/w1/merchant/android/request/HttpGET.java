package com.w1.merchant.android.request;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.w1.merchant.android.utils.NetworkUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpGET extends AsyncTask<String, Void, String[]> {
	
	Context mCtx;
	HttpClient httpclient;
	String direction;
	String currencyFilter;
	HttpGet httpget;
	HttpResponse response;
	HttpEntity httpEntity;
	String[] result = { "", "" };
		
	public HttpGET(Context ctx) {
		mCtx = ctx;
	}
	
	@Override
    protected String[] doInBackground(String... url) {
    	try {
    		httpclient = NetworkUtils.getInstance().createApacheOkHttpClient();
            //line = "";
            httpget = new HttpGet(url[0]);
            direction = url[2];
            currencyFilter= url[3];
    		httpget.setHeader("Authorization", "Bearer " + url[1]);
	        httpget.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
    		response = httpclient.execute(httpget);
    	    httpEntity = response.getEntity();
    	    //line = EntityUtils.toString(httpEntity, "UTF-8");
    	    result[0] = response.getStatusLine().getStatusCode() + "";
	        httpEntity = response.getEntity();
	        result[1] = EntityUtils.toString(httpEntity, "UTF-8");
    	    //Log.d("1", line);
    	} catch (ClientProtocolException e) {
    		Log.d("1", e + "");
    	} catch (IOException e) {
    		Log.d("1", e + "");
    	}
        return result;
    }
}
