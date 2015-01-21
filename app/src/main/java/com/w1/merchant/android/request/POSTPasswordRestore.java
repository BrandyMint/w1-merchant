package com.w1.merchant.android.request;

import android.content.Context;

import com.w1.merchant.android.utils.NetworkUtils;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class POSTPasswordRestore extends HttpPOST {

	public POSTPasswordRestore(Context ctx) {
		super(ctx);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected String[] doInBackground(String... data) {
		httpclient = NetworkUtils.getInstance().createApacheOkHttpClient();
    	httppost = new HttpPost(data[0]);
	    try {
	    	httppost.setHeader("Authorization", "Bearer {54344285-82DA-42EA-B7D0-0C9B978FFD89}");
	        httppost.setHeader("Accept", "application/vnd.wallet.openapi.v1+json");
	        httppost.setHeader("Content-Type", "application/vnd.wallet.openapi.v1+json");
	        httppost.setHeader("X-Wallet-CaptchaId", data[2]);
	        httppost.setHeader("X-Wallet-CaptchaCode", data[3]);
	        response = httpclient.execute(httppost);
	        result[0] = response.getStatusLine().getStatusCode() + "";
	        httpEntity = response.getEntity();
	        result[1] = EntityUtils.toString(httpEntity, "UTF-8");
    	} catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
		return result;
    }
}
