package com.w1.merchant.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.Invoice;
import com.w1.merchant.android.rest.model.InvoiceRequest;
import com.w1.merchant.android.ui.adapter.CurrencyAdapter;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.text.CurrencyFormattingTextWatcher;
import com.w1.merchant.android.utils.text.CurrencyKeyListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class AddInvoiceActivity extends ActivityBase implements ConfirmDialogFragment.InteractionListener {

    private static final String PHONE_PATTERN = "[0-9]{11}";
    private static final String APP_PREF = "W1_Pref";

    private static final String ARG_DEFAULT_CURRENCY = "com.w1.merchant.android.ui.AddInvoiceActivity.ARG_DEFAULT_CURRENCY";
    private static final String ARG_CURRENCY_LIST = "com.w1.merchant.android.ui.AddInvoiceActivity.ARG_CURRENCY_LIST";

    private static final String KEY_SELECTED_CURRENCY = "SELECTED_CURRENCY";
    private static final String KEY_CURRENCY_LIST = "CURRENCY_LIST";

    private AutoCompleteTextView mDescriptionView;
    private AutoCompleteTextView mPhoneView;
    private Spinner mCurrencySpinner;
    private EditText etSum;
    private ProgressBar mProgressView;

    private TextWatcher mCurencyTextWatcher;

    private Vibrator mVibrator;
    private SharedPreferences mPref;

    private CurrencyAdapter mCurrencyAdapter;

    private final ArrayList<String> mCurrencyList = new ArrayList<>();
    private final ArrayList<String> descrsArray = new ArrayList<>();
    private final ArrayList<String> telEmailArray = new ArrayList<>();

    private Subscription mCreateInvoiceSubscription = Subscriptions.unsubscribed();
    private Subscription mCurrencyListSubscription = Subscriptions.unsubscribed();

    public static void startActivity(Context context,
                                     @Nullable String selectedCurrencyId,
                                     @Nullable ArrayList<String> currencyList,
                                     @Nullable View animateFrom) {
        Intent intent = new Intent(context, AddInvoiceActivity.class);
        if (selectedCurrencyId != null) intent.putExtra(ARG_DEFAULT_CURRENCY, selectedCurrencyId);
        if (currencyList != null && ! currencyList.isEmpty()) intent.putStringArrayListExtra(ARG_CURRENCY_LIST, currencyList);

        if (!(context instanceof Activity) || animateFrom == null) {
            context.startActivity(intent);
            return;
        }
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                animateFrom, 0, 0, animateFrom.getWidth(), animateFrom.getHeight());
        ActivityCompat.startActivity((Activity)context, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_invoice);
        mDescriptionView = (AutoCompleteTextView) findViewById(R.id.actvDescr);
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.actvTelEmail);
        etSum = (EditText) findViewById(R.id.etSum);
        mProgressView = (ProgressBar) findViewById(R.id.ab2_progress);
        mCurrencySpinner = (Spinner)findViewById(R.id.currency_spinner);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        TextView tvBillButton = (TextView) findViewById(R.id.tvBillButton);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        mPref = getSharedPreferences(APP_PREF, MODE_PRIVATE);

        setSupportActionBar(toolbar);
        int abOptions =  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP;
        getSupportActionBar().setDisplayOptions(abOptions, abOptions);

        descrsArray.addAll(mPref.getStringSet("descrs", Collections.<String>emptySet()));
        telEmailArray.addAll(mPref.getStringSet("telEmail", Collections.<String>emptySet()));

        String selectedCurrency = loadSavedSelectedCurrency(savedInstanceState, getIntent());
        mCurrencyList.clear();
        mCurrencyList.addAll(loadSavedBalanceList(savedInstanceState, getIntent()));

        ((TextView) toolbar.findViewById(R.id.ab2_title)).setText(R.string.bill);

        mDescriptionView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, descrsArray));
        mDescriptionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPhoneView.requestFocus();
            }
        });


        mPhoneView.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, telEmailArray));
        mPhoneView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etSum.requestFocus();
            }
        });

        initCurrencySpinner(selectedCurrency);
        if (selectedCurrency != null) {
            mCurencyTextWatcher = new CurrencyFormattingTextWatcher(selectedCurrency);
            etSum.setKeyListener(new CurrencyKeyListener(selectedCurrency));
            etSum.addTextChangedListener(new CurrencyFormattingTextWatcher(selectedCurrency));
        } else {
            etSum.setEnabled(false);
        }
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

        tvBillButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bill();
            }
        });

        if (mCurrencyList.isEmpty()) reloadCurrencyList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String selectedCurrency = (String)mCurrencySpinner.getSelectedItem();
        if (selectedCurrency != null) outState.putString(KEY_SELECTED_CURRENCY, selectedCurrency);
        if (!mCurrencyList.isEmpty()) outState.putStringArrayList(KEY_CURRENCY_LIST, mCurrencyList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCreateInvoiceSubscription.unsubscribe();
        mCurrencyListSubscription.unsubscribe();
    }

    @Nullable
    private String loadSavedSelectedCurrency(@Nullable Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            String currency = savedInstanceState.getString(KEY_SELECTED_CURRENCY, null);
            if (currency != null) return currency;
        }

        return intent.getStringExtra(ARG_DEFAULT_CURRENCY);
    }

    private List<String> loadSavedBalanceList(@Nullable Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState != null) {
            ArrayList<String> listSavedInstance = savedInstanceState.getStringArrayList(KEY_CURRENCY_LIST);
            if (listSavedInstance != null) return listSavedInstance;
        }

        ArrayList<String> listIntent = intent.getStringArrayListExtra(ARG_CURRENCY_LIST);
        if (listIntent != null) {
            return listIntent;
        } else {
            return Collections.emptyList();
        }
    }

    private void initCurrencySpinner(@Nullable String selectedCurrency) {
        mCurrencyAdapter = new CurrencyAdapter(this, mCurrencyList,
                R.layout.item_spinner_currency, android.R.layout.simple_spinner_dropdown_item);
        mCurrencySpinner.setAdapter(mCurrencyAdapter);
        mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = (String)parent.getItemAtPosition(position);
                BigDecimal amount = CurrencyHelper.parseAmount(etSum.getText());
                etSum.setKeyListener(new CurrencyKeyListener(selectedCurrency));
                if (mCurencyTextWatcher != null) {
                    etSum.removeTextChangedListener(mCurencyTextWatcher);
                }
                mCurencyTextWatcher = new CurrencyFormattingTextWatcher(selectedCurrency);
                etSum.addTextChangedListener(mCurencyTextWatcher);
                if (amount != null) etSum.setText(amount.toPlainString());
                etSum.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (selectedCurrency != null) {
            int selectedPosition = mCurrencyList.indexOf(selectedCurrency);
            if (selectedPosition >= 0) mCurrencySpinner.setSelection(selectedPosition);
        }
    }

    boolean checkFields() {
        int err = 0;
        if (TextUtils.isEmpty(mPhoneView.getText().toString())) {
            if (err == 0) mPhoneView.setError(getText(R.string.error_input_field_must_no_be_empty));
            err += 1;
        } else {
            if ((mPhoneView.getText().toString().indexOf("@") <= 0) &&
                    (!mPhoneView.getText().toString().matches(PHONE_PATTERN))) {
                mPhoneView.setError(getText(R.string.mail_or_tel));
                err += 1;
            }
        }
        if (TextUtils.isEmpty(etSum.getText().toString())) {
            if (err == 0) etSum.setError(getText(R.string.error_input_field_must_no_be_empty));
            err += 1;
        }
        return (err == 0);
    }

    private void setupLoadingState() {
        if (isFinishing()) return;
        boolean isLoading = !mCurrencyListSubscription.isUnsubscribed()
                || !mCreateInvoiceSubscription.isUnsubscribed();
        mProgressView.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
    }

    private void reloadCurrencyList() {
        if (!mCurrencyListSubscription.isUnsubscribed()) return;

        rx.Observable<List<Balance>> observable = RestClient.getApiBalance().getBalance();
        mCurrencyListSubscription = AppObservable.bindActivity(this, observable)
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupLoadingState();
                    }
                })
                .subscribe(new Observer<List<Balance>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        CharSequence errText = ((ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(AddInvoiceActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                        if (mCurrencyList.isEmpty()) {
                            mCurrencyList.add(CurrencyHelper.ROUBLE_CURRENCY_NUMBER);
                            mCurrencyAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onNext(List<Balance> balances) {
                        if (mCurrencySpinner == null) return;
                        String oldSelectedCurrencyId = (String) mCurrencySpinner.getSelectedItem();

                        mCurrencyList.clear();
                        for (Balance balance : balances) mCurrencyList.add(balance.currencyId);
                        mCurrencyAdapter.notifyDataSetChanged();

                        int newCurrencyPos = -1;
                        if (oldSelectedCurrencyId != null)
                            newCurrencyPos = mCurrencyList.indexOf(oldSelectedCurrencyId);
                        if (newCurrencyPos < 0) {
                            int nativeCurrencyPos = -1;
                            for (int i = 0, size = balances.size(); i < size; ++i) {
                                if (balances.get(i).isNative) {
                                    nativeCurrencyPos = i;
                                    break;
                                }
                            }
                            if (nativeCurrencyPos >= 0) newCurrencyPos = nativeCurrencyPos;
                        }
                        if (newCurrencyPos >= 0) {
                            mCurrencySpinner.setSelection(newCurrencyPos);
                        }
                    }
                });
        setupLoadingState();
    }

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
            BigDecimal amount = CurrencyHelper.parseAmount(etSum.getText());
            if (amount == null) {
                etSum.setError(getString(R.string.error_input_field_must_no_be_empty));
                return;
            }

            String currency = (String)mCurrencySpinner.getSelectedItem();
            if (currency == null) currency = CurrencyHelper.ROUBLE_CURRENCY_NUMBER;

            createInvoice(recipient, amount, currency, description);
        }
    }

    private void createInvoice(String recipient,
                               BigDecimal amount,
                               String currency,
                               String description) {

        mCreateInvoiceSubscription.unsubscribe();
        Observable<Invoice> observer = AppObservable.bindActivity(this,
                RestClient.getApiInvoices().createInvoice(new InvoiceRequest(recipient,
                        amount, description, currency)));

        mCreateInvoiceSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupLoadingState();
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
                        String amount = CurrencyHelper.formatAmount(invoice.amount, invoice.currencyId);
                        String description = getString(R.string.invoice_issued_successfully, amount);
                        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(description);
                        FragmentManager fm = getSupportFragmentManager();
                        Fragment old = fm.findFragmentByTag("CONFIRM_DIALOG_FRAGMENT");
                        if (old != null) return;
                        dialog.show(fm, "CONFIRM_DIALOG_FRAGMENT");
                    }
                });
        setupLoadingState();
    }

    @Override
    public void onConfirmDialogDismissed() {
        finish();
        overridePendingTransition(0, 0);
    }
}

