package ru.bokus.w1.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.bokus.w1.Request.JSONParsing;
import ru.bokus.w1.Request.POSTInvoices;
import ru.bokus.w1.Request.Urls;
import ru.bokus.w1.ViewExtended.EditTextRouble;

public class AddInvoice extends Activity {

	TextView tvBillButton;
	EditTextRouble etSum;
	ImageView ivBack;
	Context context;
	POSTInvoices postInvoices;
	String[] requestData = {"", "", "", "", "", ""};
	String[] httpResult = {"", ""};
	Intent intent;
	String token;
	ProgressBar pbInvoice;
	String pattern = "[0-9]{11}";
	Vibrator vibro;
	long milliseconds = 100;
	public final String APP_PREF = "W1_Pref";
	SharedPreferences sPref;
	AutoCompleteTextView actvDescr, actvTelEmail;
	Set<String> descrs = new HashSet<String>();
	ArrayList<String> descrsArray = new ArrayList<String>();
	Set<String> telEmail = new HashSet<String>();
	ArrayList<String> telEmailArray = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invoice_add);
		context = this;
		
		sPref = getSharedPreferences(APP_PREF, MODE_PRIVATE);
		actvDescr = (AutoCompleteTextView) findViewById(R.id.actvDescr);
		descrs = sPref.getStringSet("descrs", new HashSet<String>());
		for(String r : descrs) {
			descrsArray.add(r);
		}
		actvDescr.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_dropdown_item_1line, descrsArray));
		
		actvTelEmail = (AutoCompleteTextView) findViewById(R.id.actvTelEmail);
		telEmail = sPref.getStringSet("telEmail", new HashSet<String>());
		for(String r : telEmail) {
			telEmailArray.add(r);
		}
		actvTelEmail.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_dropdown_item_1line, telEmailArray));
		
		intent = getIntent();
		token = intent.getStringExtra("token");
		etSum = (EditTextRouble) findViewById(R.id.etSum);
		etSum.addTextChangedListener(new CurrencyTextWatcher());
		//DigitsKeyListener digkl1 = DigitsKeyListener.getInstance(true, true);
		//etSum.setKeyListener(digkl1);
		tvBillButton = (TextView) findViewById(R.id.tvBillButton);
		tvBillButton.setOnClickListener(myOnClickListener);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(myOnClickListener);
		pbInvoice = (ProgressBar) findViewById(R.id.pbInvoice);
		vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}	
	
	public boolean checkFields() {
		int err = 0;
		if (TextUtils.isEmpty(actvDescr.getText().toString())) {
			actvDescr.setError(getString(R.string.error_field));
			err = 1;
		}
		if (TextUtils.isEmpty(actvTelEmail.getText().toString())) {
			if (err == 0) actvTelEmail.setError(getString(R.string.error_field));
			err += 1;
		}
		if ((actvTelEmail.getText().toString().indexOf("@") > 0) |
				(actvTelEmail.getText().toString().matches(pattern))) {
		} else {
			if (err == 0) etSum.setError(getString(R.string.mail_or_tel));
			err += 1;
		}
		if (TextUtils.isEmpty(etSum.getText().toString())) {
			if (err == 0) etSum.setError(getString(R.string.error_field));
			err += 1;
		} 
		return (err == 0);
	}
	
	OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case (R.id.tvBillButton):
				if (checkFields()) {
					vibro.vibrate(milliseconds);
					
					Editor ed = sPref.edit();
					descrsArray.add(actvDescr.getText().toString());
					Set<String> newDescr = new HashSet<String>();
					for (String n : descrsArray) {
						newDescr.add(n);
					}
					ed.putStringSet("descrs", newDescr);
					
					telEmailArray.add(actvTelEmail.getText().toString());
					Set<String> newTelEmail = new HashSet<String>();
					for (String n : telEmailArray) {
						newTelEmail.add(n);
					}
					ed.putStringSet("telEmail", newTelEmail);
					ed.apply();
					
					pbInvoice.setVisibility(View.VISIBLE);
					requestData[0] = Urls.URL + Urls.URL_NEW_INVOICE;
					requestData[1] = token; 
	      			requestData[2] = etSum.getText().toString().replaceAll("C", "");
	      			requestData[3] = actvDescr.getText().toString();
	      			requestData[4] = actvTelEmail.getText().toString();
	      			postInvoices = new POSTInvoices(context);
	    			postInvoices.execute(requestData);
	    		}
				break;
			case (R.id.ivBack):
				finish();
				break;
			}
		}
	};
	
	public void setInvoicesResult(String[] httpRes) {
		pbInvoice.setVisibility(View.INVISIBLE);
		if (httpRes[0].equals("201")) {
			intent = new Intent(context, ConfirmActivity.class);
			intent.putExtra("sum", etSum.getText().toString());
			startActivity(intent);
			finish();
		} else {
			Toast toast = Toast.makeText(context, 
					JSONParsing.invoiceError(httpRes[1]), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		}
	}
	
	public class CurrencyTextWatcher implements TextWatcher {
	    boolean mEditing;
	    
	    public CurrencyTextWatcher() {
	    	mEditing = false;
	    }
	    
	    public synchronized void afterTextChanged(Editable s) {
	        if (!mEditing) {
	            mEditing = true;
	            String digits = s.toString().replaceAll(" C", "");
	            s.replace(0, s.length(), digits + " C");
	            mEditing = false;
	        }
	    }
	    
	    public void beforeTextChanged(CharSequence s, int start, int count,
	    		int after) {
	    }
	    
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	    }
	}
}

