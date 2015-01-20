package ru.bokus.w1.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class Details extends Activity {

	TextView tvSum, tvAccountNumber, tvStatus, tvDescr, tvRub;
	ImageView ivBack, ivStatusIcon;
	Intent intent;
	String number, currency;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.details);
		intent = getIntent();
		number = intent.getStringExtra("number");
		tvSum = (TextView) findViewById(R.id.tvSum);
		tvRub = (TextView) findViewById(R.id.tvRub);
		currency = intent.getStringExtra("currency");
		if (currency.equals("643")) {
			tvRub.setText("B");
		} else {
			tvRub.setText("");
		}
		tvAccountNumber = (TextView) findViewById(R.id.tvAccountNumber);
		tvStatus = (TextView) findViewById(R.id.tvStatus);
		tvDescr = (TextView) findViewById(R.id.tvDescr);
		if (number.equals("Вывод средств")) {
			tvSum.setText("-" + intent.getStringExtra("amount"));
		} else {
			tvSum.setText(intent.getStringExtra("amount"));
		}
		tvAccountNumber.setText(number);
		String tvStatusText = intent.getStringExtra("state") + ", " +
				intent.getStringExtra("date");
		tvStatusText = tvStatusText.replace(getString(R.string.moment_ago),
				getString(R.string.min_ago2));
		tvStatus.setText(tvStatusText);
		tvDescr.setText(intent.getStringExtra("descr"));
		ivStatusIcon = (ImageView) findViewById(R.id.ivStatusIcon);
		if (!intent.getStringExtra("state").equals(getString(R.string.paid))) {
			ivStatusIcon.setImageResource(R.drawable.icon_progress_big);
			tvSum.setTextColor(Color.parseColor("#FFA726"));
			tvRub.setTextColor(Color.parseColor("#FFA500"));
		}
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(myOnClickListener);
	}	
	
	OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case (R.id.ivBack):
				finish();
				break;
			}
		}
	};
}

