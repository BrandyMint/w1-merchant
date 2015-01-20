package ru.bokus.w1.Activity;

import ru.bokus.w1.Request.JSONParsing;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class UserEntryTotal extends Activity {

	TextView tvSumIn, tvComisIn, tvSumOut, tvComisOut;
	TextView tvSumInRubl, tvComisInRubl, tvSumOutRubl, tvComisOutRubl;
	ImageView ivBack;
	Intent intent;
	String currency;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.userentry_total);
		intent = getIntent();
		currency = intent.getStringExtra("nativeCurrency");
		if (currency.equals("643")) {
			currency = "";
			tvSumInRubl = (TextView) findViewById(R.id.tvSumInRubl);
			tvSumInRubl.setText("C");
			tvSumOutRubl = (TextView) findViewById(R.id.tvSumOutRubl);
			tvSumOutRubl.setText("C");
			tvComisInRubl = (TextView) findViewById(R.id.tvComisInRubl);
			tvComisInRubl.setText("C");
			tvComisOutRubl = (TextView) findViewById(R.id.tvComisOutRubl);
			tvComisOutRubl.setText("C");
		} else {
			currency = JSONParsing.getCurrencySymbol(currency);
		}
			
			tvSumIn = (TextView) findViewById(R.id.tvSumIn);
			tvSumIn.setText(getString(R.string.sum_period) + " " +
					intent.getIntExtra("SumIn", 0) + " " + currency);
			
			tvSumOut = (TextView) findViewById(R.id.tvSumOut);
			tvSumOut.setText(getString(R.string.sum_period) + " " +
					intent.getIntExtra("SumOut", 0) + " " + currency);
			
			tvComisIn = (TextView) findViewById(R.id.tvComisIn);
			tvComisIn.setText(getString(R.string.comis_period) + " " +
					intent.getIntExtra("ComisIn", 0) + " " + currency);
			
			tvComisOut = (TextView) findViewById(R.id.tvComisOut);
			tvComisOut.setText(getString(R.string.comis_period) + " " +
					intent.getIntExtra("ComisOut", 0) + " " + currency);
		
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

