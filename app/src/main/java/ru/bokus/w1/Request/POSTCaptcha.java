package ru.bokus.w1.Request;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import ru.bokus.w1.Activity.LoginActivity;
import android.content.Context;
import android.view.View;

public class POSTCaptcha extends HttpPOST {

	public POSTCaptcha(Context ctx) {
		super(ctx);
	}

	@Override
    protected String[] doInBackground(String... data) {
		httpclient = LoginActivity.httpclient;
    	httppost = new HttpPost(data[0]);
	    try {
	        httppost.setHeader("Authorization", "Bearer {54344285-82DA-42EA-B7D0-0C9B978FFD89}");
	        httppost.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
//	        nameValuePairs.add(new BasicNameValuePair("Width", data[1]));
//	    	nameValuePairs.add(new BasicNameValuePair("Height",  data[2]));
//	    	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"utf-8"));
	    	response = httpclient.execute(httppost);
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
    	//((LoginActivity) mCtx).captchaResult(result);
    	((LoginActivity) mCtx).pbLogin.setVisibility(View.INVISIBLE);
    }
}
