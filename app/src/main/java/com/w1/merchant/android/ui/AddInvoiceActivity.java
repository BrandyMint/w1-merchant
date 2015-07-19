package com.w1.merchant.android.ui;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Invoice;
import com.w1.merchant.android.rest.model.InvoiceRequest;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class AddInvoiceActivity extends ActivityBase {

    private static final String PHONE_PATTERN = "[0-9]{11}";
    private static final String APP_PREF = "W1_Pref";

    private EditText etSum;
    private ProgressBar pbInvoice;

    private Vibrator mVibrator;

    private SharedPreferences mPref;
    private AutoCompleteTextView mDescriptionView;
    private AutoCompleteTextView mPhoneView;

    private final ArrayList<String> descrsArray = new ArrayList<>();
    private final ArrayList<String> telEmailArray = new ArrayList<>();

    private Subscription mCreateInvoiceSubscription = Subscriptions.unsubscribed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_invoice);

        mPref = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        mDescriptionView = (AutoCompleteTextView) findViewById(R.id.actvDescr);
        Set<String> descrs = mPref.getStringSet("descrs", new HashSet<String>());
        for(String r : descrs) {
            descrsArray.add(r);
        }
        mDescriptionView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, descrsArray));
        mDescriptionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPhoneView.requestFocus();
            }
        });

        mPhoneView = (AutoCompleteTextView) findViewById(R.id.actvTelEmail);
        Set<String> telEmail = mPref.getStringSet("telEmail", new HashSet<String>());
        for(String r : telEmail) {
            telEmailArray.add(r);
        }
        mPhoneView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, telEmailArray));
        mPhoneView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etSum.requestFocus();
            }
        });

        etSum = (EditText) findViewById(R.id.etSum);
        etSum.addTextChangedListener(new TextWatcher() {
            boolean mEditing = false;

            public synchronized void afterTextChanged(Editable s) {
                if (!mEditing) {
                    if (!s.toString().endsWith(" " + CurrencyHelper.ROUBLE_SYMBOL)) {
                        mEditing = true;
                        etSum.setText(s.toString().replaceAll("[^0-9]+", "") + " " + CurrencyHelper.ROUBLE_SYMBOL);
                                etSum.setSelection(etSum.getText().length() - 2);
                        mEditing = false;
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        etSum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    bill();
                    return true;
                }
                return false;
            }
        });
        TextView tvBillButton = (TextView) findViewById(R.id.tvBillButton);
        tvBillButton.setOnClickListener(myOnClickListener);
        findViewById(R.id.ivBack).setOnClickListener(myOnClickListener);
        pbInvoice = (ProgressBar) findViewById(R.id.pbInvoice);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCreateInvoiceSubscription.unsubscribe();
    }

    boolean checkFields() {
        int err = 0;
        if (TextUtils.isEmpty(mPhoneView.getText().toString())) {
            if (err == 0) mPhoneView.setError(getText(R.string.error_field));
            err += 1;
        } else {
            if ((mPhoneView.getText().toString().indexOf("@") <= 0) &&
                    (!mPhoneView.getText().toString().matches(PHONE_PATTERN))) {
                mPhoneView.setError(getText(R.string.mail_or_tel));
                err += 1;
            }
        }
        if (TextUtils.isEmpty(etSum.getText().toString())) {
            if (err == 0) etSum.setError(getText(R.string.error_field));
            err += 1;
        }
        return (err == 0);
    }

    private final OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case (R.id.tvBillButton):
                bill();
                break;
            case (R.id.ivBack):
                finish();
                break;
            }
        }
    };

    private void bill() {
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
                amount = new BigDecimal(etSum.getText().toString().replaceAll("[ \\u20bd]+", ""));
            } catch (NumberFormatException ne) {
                etSum.setError(getString(R.string.error_field));
                return;
            }
            createInvoice(recipient, amount, description);
        }
    }

    private void createInvoice(final String recipient, final BigDecimal amount, final String description) {
        pbInvoice.setVisibility(View.INVISIBLE);

        mCreateInvoiceSubscription.unsubscribe();
        Observable<Invoice> observer = AppObservable.bindActivity(this,
                RestClient.getApiInvoices().createInvoice(new InvoiceRequest(recipient,
                        amount, description, "643")));

        mCreateInvoiceSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        pbInvoice.setVisibility(View.VISIBLE);
                    }
                })
                .subscribe(new Observer<Invoice>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        CharSequence errText = ((ResponseErrorException)e).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(AddInvoiceActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(Invoice invoice) {
                        Intent intent = new Intent(AddInvoiceActivity.this, ConfirmInvoiceActivity.class);
                        intent.putExtra("sum", etSum.getText().toString());
                        startActivity(intent);
                        finish();
                    }
                });
    }
}

