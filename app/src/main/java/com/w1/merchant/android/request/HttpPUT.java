package com.w1.merchant.android.request;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.w1.merchant.android.activity.ConfirmOutActivity;
import com.w1.merchant.android.activity.LoginActivity;
import android.content.Context;
import android.os.AsyncTask;

public class HttpPUT extends AsyncTask<String, Void, String[]> {
	
	public final Context mCtx;
	HttpClient httpclient;
	HttpPut httpput;
	String[] result = { "", "" };
	//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);	
	HttpResponse response = null;
	HttpEntity httpEntity;
	JSONObject jsonObj = new JSONObject();
	
	JSONArray jArray = new JSONArray();
	ArrayList<String[]> dataPayments;
	
	public HttpPUT(Context ctx) {
		mCtx = ctx;
	}
	
    @Override
    protected String[] doInBackground(String... data) {
    	dataPayments = ((ConfirmOutActivity) mCtx).getDataPayment();
    	httpclient = LoginActivity.httpclient;
		httpput = new HttpPut(data[0]);
	    try {
	    	httpput.setHeader("Authorization", "Bearer " + data[1]);
	        httpput.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
	        httpput.setHeader("Content-Type", "application/vnd.wallet.openapi.v1+json");
	        //Log.d("1", "Bearer " + data[1]);
	        for (int i = 2; i < dataPayments.size(); i++) {
	        	JSONObject jsonParams = new JSONObject();
	        	jsonParams.put("FieldId", dataPayments.get(i)[0]);
	        	jsonParams.put("Value", dataPayments.get(i)[1]);
	        	jArray.put(jsonParams);
	        }
	        jsonObj.put("FormId", dataPayments.get(1)[1]);
	        jsonObj.put("Params", jArray);
	        
	        httpput.setEntity(new StringEntity(jsonObj.toString(), HTTP.UTF_8));
	        
	        response = httpclient.execute(httpput);
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
