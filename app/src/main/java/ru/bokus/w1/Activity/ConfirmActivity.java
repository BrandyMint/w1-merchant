package ru.bokus.w1.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfirmActivity extends Activity {

	EditText etDescrText, etTelRecipient, etSum;
	TextView tvBack, tvConfirmText;
	ImageView ivBack;
	Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.confirmation);
		
		intent = getIntent();
		tvConfirmText = (TextView) findViewById(R.id.tvConfirmText);
		tvConfirmText.setText(getString(R.string.account_sum) + " " +
				intent.getStringExtra("sum") + " " + getString(R.string.bill_success));
		
		tvBack = (TextView) findViewById(R.id.tvBack);
		tvBack.setOnClickListener(myOnClickListener);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(myOnClickListener);
	}	
	
	OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}

