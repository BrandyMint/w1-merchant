package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;

public class Captcha extends Activity {

	EditText etCaptcha;
	TextView tvSend;
	Intent intent;
	Context context;
	WebView wvCaptcha;
	String id;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.captcha);
		context = this;
		
		intent = getIntent();
		etCaptcha = (EditText) findViewById(R.id.etCaptcha);
		tvSend = (TextView) findViewById(R.id.tvSend);
		wvCaptcha = (WebView) findViewById(R.id.wvCaptcha);
		wvCaptcha.loadUrl(intent.getStringExtra("url"));
		id = intent.getStringExtra("id");
				
		tvSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!TextUtils.isEmpty(etCaptcha.getText().toString())) {
					intent = new Intent();
					intent.putExtra("captchaText", etCaptcha.getText().toString());
					intent.putExtra("captchaId", id);
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Toast.makeText(context, getString(R.string.input_captcha),
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	

}
