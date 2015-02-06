package com.w1.merchant.android.activity;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.w1.merchant.android.R;
import com.w1.merchant.android.model.Invoice;
import com.w1.merchant.android.model.InvoiceRequest;
import com.w1.merchant.android.service.ApiInvoices;
import com.w1.merchant.android.service.ApiRequestTaskActivity;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.viewextended.EditTextRouble;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import retrofit.Callback;
import retrofit.client.Response;

public class AddInvoice extends Activity {

    private static final String PHONE_PATTERN = "[0-9]{11}";
    private static final String APP_PREF = "W1_Pref";

    private EditTextRouble etSum;
	private ProgressBar pbInvoice;

	private Vibrator mVibrator;

	private SharedPreferences mPref;
	private AutoCompleteTextView mDescriptionView;
    private AutoCompleteTextView mPhoneView;

    private final ArrayList<String> descrsArray = new ArrayList<>();
    private final ArrayList<String> telEmailArray = new ArrayList<>();

    private ApiInvoices mApiInvoices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invoice_add);

        mApiInvoices = NetworkUtils.getInstance().createRestAdapter().create(ApiInvoices.class);

		mPref = getSharedPreferences(APP_PREF, MODE_PRIVATE);
		mDescriptionView = (AutoCompleteTextView) findViewById(R.id.actvDescr);
        Set<String> descrs = mPref.getStringSet("descrs", new HashSet<String>());
		for(String r : descrs) {
			descrsArray.add(r);
		}
		mDescriptionView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, descrsArray));
		
		mPhoneView = (AutoCompleteTextView) findViewById(R.id.actvTelEmail);
        Set<String> telEmail = mPref.getStringSet("telEmail", new HashSet<String>());
		for(String r : telEmail) {
			telEmailArray.add(r);
		}
		mPhoneView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, telEmailArray));

		etSum = (EditTextRouble) findViewById(R.id.etSum);
		etSum.addTextChangedListener(new CurrencyTextWatcher());
		//DigitsKeyListener digkl1 = DigitsKeyListener.getInstance(true, true);
		//etSum.setKeyListener(digkl1);
        TextView tvBillButton = (TextView) findViewById(R.id.tvBillButton);
		tvBillButton.setOnClickListener(myOnClickListener);
		findViewById(R.id.ivBack).setOnClickListener(myOnClickListener);
		pbInvoice = (ProgressBar) findViewById(R.id.pbInvoice);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}	
	
	boolean checkFields() {
		int err = 0;
		if (TextUtils.isEmpty(mDescriptionView.getText().toString())) {
			mDescriptionView.setError(getString(R.string.error_field));
			err = 1;
		}
		if (TextUtils.isEmpty(mPhoneView.getText().toString())) {
			if (err == 0) mPhoneView.setError(getString(R.string.error_field));
			err += 1;
		}
		if ((mPhoneView.getText().toString().indexOf("@") > 0) |
				(mPhoneView.getText().toString().matches(PHONE_PATTERN))) {
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
	
	private final OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case (R.id.tvBillButton):
				if (checkFields()) {
                    long milliseconds = 100;
                    mVibrator.vibrate(milliseconds);
					
					Editor ed = mPref.edit();
					descrsArray.add(mDescriptionView.getText().toString());
					Set<String> newDescr = new HashSet<>();
					for (String n : descrsArray) {
						newDescr.add(n);
					}
					ed.putStringSet("descrs", newDescr);
					
					telEmailArray.add(mPhoneView.getText().toString());
					Set<String> newTelEmail = new HashSet<>();
					for (String n : telEmailArray) {
						newTelEmail.add(n);
					}
					ed.putStringSet("telEmail", newTelEmail);
					ed.apply();

                    String recipient = mPhoneView.getText().toString();
                    String description =  mDescriptionView.getText().toString();
                    BigDecimal amount;

                    try {
                        amount = new BigDecimal(etSum.getText().toString().replaceAll("C", ""));
                    } catch (NumberFormatException ne) {
                        etSum.setError(getString(R.string.error_field));
                        break;
                    }
                    createInvoice(recipient, amount, description);
	    		}
				break;
			case (R.id.ivBack):
				finish();
				break;
			}
		}
	};

    private void createInvoice(final String recipient, final BigDecimal amount, final String description) {

        pbInvoice.setVisibility(View.INVISIBLE);
        new ApiRequestTaskActivity<Invoice>(this, R.string.network_error) {

            @Override
            protected void doRequest(Callback<Invoice> callback) {
                mApiInvoices.createInvoice(new InvoiceRequest(recipient, amount, description, "643"), callback);
            }

            @Override
            protected void stopAnimation() {
                pbInvoice.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onSuccess(Invoice invoice, Response response, Activity activity) {
                Intent intent = new Intent(AddInvoice.this, ConfirmActivity.class);
                intent.putExtra("sum", etSum.getText().toString());
                startActivity(intent);
                finish();
            }


        }.execute();
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

