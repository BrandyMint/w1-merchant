package com.w1.merchant.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.w1.merchant.android.R;

public class ConfirmPayment extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.confirmation);

		TextView tvConfirmText = (TextView) findViewById(R.id.tvConfirmText);
		tvConfirmText.setText(getString(R.string.transact_proces, 
				getIntent().getStringExtra("sum") + " C"));
		
		findViewById(R.id.tvBack).setOnClickListener(myOnClickListener);
		findViewById(R.id.ivBack).setOnClickListener(myOnClickListener);
	}	
	
	private final OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}

