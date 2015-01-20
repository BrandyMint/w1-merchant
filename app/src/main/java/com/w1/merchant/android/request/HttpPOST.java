package com.w1.merchant.android.request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class HttpPOST extends AsyncTask<String, Void, String[]> {
	
	public final Context mCtx;
	HttpClient httpclient;
	HttpPost httppost;
	String[] result = { "", "" };
	//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);	
	HttpResponse response = null;
	HttpEntity httpEntity;
	JSONObject jsonObj = new JSONObject();
	
	public HttpPOST(Context ctx) {
		mCtx = ctx;
	}
	
    @Override
    protected String[] doInBackground(String... data) {
    	return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
    	super.onPostExecute(result);    
    	
    }

    @Override
    protected void onPreExecute() {
   		super.onPreExecute();
    }
    
    @Override
    protected void onCancelled() {
    	super.onCancelled();
    }
}
