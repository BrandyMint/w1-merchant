package com.w1.merchant.android.request;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

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
}
