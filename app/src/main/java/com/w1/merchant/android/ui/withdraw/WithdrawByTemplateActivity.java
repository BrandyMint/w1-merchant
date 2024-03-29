package com.w1.merchant.android.ui.withdraw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.Template;
import com.w1.merchant.android.ui.ActivityBase;
import com.w1.merchant.android.utils.text.CurrencyFormattingTextWatcher;
import com.w1.merchant.android.utils.text.CurrencyKeyListener;
import com.w1.merchant.android.utils.text.TextWatcherWrapper;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Вывод по шаблону
 */
public class WithdrawByTemplateActivity extends ActivityBase {
    private static final String TAG = "WithdrawByTemplateAct";
    private static final boolean DBG = BuildConfig.DEBUG;

    private EditText mAmountEditText;
    private EditText mCommissionEditText;
    private TextView titleView;
    private ImageView iconView;
    private ProgressBar progressView;
    private String templateId;
    private boolean mIsBusinessAccount;
    private LinearLayout llMain;
    private int totalReq = 0;
    private LinearLayout.LayoutParams lParams4;
    private String mSum = "";

    private Provider mProvider;

    private TextWatcherWrapper mAmountTextWatcherWrapper;
    private TextWatcherWrapper mCommissionTextWatcherWrapper;

    private Subscription mGetProviderSubscription = Subscriptions.empty();
    private Subscription mGetTemplateSubscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_by_template);

        llMain = (LinearLayout) findViewById(R.id.main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        iconView = (ImageView) findViewById(R.id.icon);
        titleView = (TextView) toolbar.findViewById(R.id.ab2_title);
        progressView = (ProgressBar) toolbar.findViewById(R.id.ab2_progress);

        setSupportActionBar(toolbar);
        int abOptions =  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP;
        getSupportActionBar().setDisplayOptions(abOptions, abOptions);


        templateId = getIntent().getStringExtra("templateId");
        mIsBusinessAccount =  getIntent().getBooleanExtra("mIsBusinessAccount", false);

        mAmountTextWatcherWrapper = new TextWatcherWrapper(new TextWatcherWrapper.OnTextChangedListener() {
            @Override
            public void onTextChanged(Editable s) {
                llMain.removeCallbacks(mAfterSumChangeRunnable);
                llMain.post(mAfterSumChangeRunnable);
            }

            private final Runnable mAfterSumChangeRunnable = new Runnable() {
                @Override
                public void run() {
                    afterSumChange();
                }
            };
        });
        mCommissionTextWatcherWrapper = new TextWatcherWrapper(new TextWatcherWrapper.OnTextChangedListener() {
            @Override
            public void onTextChanged(Editable s) {
                llMain.removeCallbacks(mAfterFullSummChangeRunnable);
                llMain.post(mAfterFullSummChangeRunnable);
            }

            private final Runnable mAfterFullSummChangeRunnable = new Runnable() {
                @Override
                public void run() {
                    afterComisChange();
                }
            };
        });

        loadTemplate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetProviderSubscription.unsubscribe();
        mGetTemplateSubscription.unsubscribe();
    }

    private void loadTemplate() {
        mGetTemplateSubscription.unsubscribe();
        startPBAnim();

        Observable<Template> observable = AppObservable.bindActivity(this,
                RestClient.getApiPayments().getTemplate(templateId));

        mGetTemplateSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        stopPBAnim();
                    }
                }).subscribe(new Observer<Template>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "template load error", e);
                        CharSequence errText = ((ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(WithdrawByTemplateActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(Template template) {
                        onTemplateLoaded(template);
                    }
                });
    }

    private void loadProvider(final String providerId) {
        mGetProviderSubscription.unsubscribe();
        startPBAnim();

        Observable<Provider> observable = AppObservable.bindActivity(this,
                RestClient.getApiPayments().getProvider(providerId));

        mGetProviderSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        stopPBAnim();
                    }
                })
                .subscribe(new Observer<Provider>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "template load error", e);
                        CharSequence errText = ((ResponseErrorException)e).getErrorDescription(getText(R.string.network_error));
                        Toast toast = Toast.makeText(WithdrawByTemplateActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(Provider provider) {
                        onProviderLoaded(provider);
                    }
                });
    }

    boolean checkFields() {
        boolean result;

        if (CurrencyHelper.parseAmount(mCommissionEditText.getText()) == null) {
            mCommissionEditText.setError(getString(R.string.error_input_field_must_no_be_empty));
            result = false;
        } else result = true;
        if (CurrencyHelper.parseAmount(mAmountEditText.getText()) == null) {
            if (result)	mAmountEditText.setError(getString(R.string.error_input_field_must_no_be_empty));
            result = false;
        } else result = true;

        return result;
    }

    //ответ на запрос Template
    private void onTemplateLoaded(Template template) {
        loadProvider(template.providerId);

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.gravity = Gravity.LEFT;
        lParams.topMargin = 40;
        lParams.leftMargin = 30;
        lParams.rightMargin = 30;

        LinearLayout.LayoutParams lParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams2.gravity = Gravity.LEFT;
        lParams2.leftMargin = 30;
        lParams2.rightMargin = 30;

        if (template.fields != null) {
            for (Template.Field field : template.fields) {
                if (!field.fieldTitle.startsWith("Сумма")) {
                    TextView tvNew = new TextView(this);
                    tvNew.setText(field.fieldTitle);
                    tvNew.setTextColor(Color.parseColor("#BDBDBD"));
                    CalligraphyUtils.applyFontToTextView(this, tvNew, Constants.FONT_REGULAR);
                    llMain.addView(tvNew, lParams);
                    TextView tvNew2 = new TextView(this);
                    tvNew2.setText(field.fieldValue);
                    tvNew2.setTextSize(22);
                    CalligraphyUtils.applyFontToTextView(this, tvNew2, Constants.FONT_REGULAR);
                    llMain.addView(tvNew2, lParams2);
                } else {
                    mSum += field.fieldValue;
                }
            }
        }

        if (!mIsBusinessAccount) {
            //сумма с комиссией
            TextView tvSumCommis = new TextView(this);
            tvSumCommis.setText(R.string.sum_commis);
            tvSumCommis.setTextColor(Color.parseColor("#BDBDBD"));
            CalligraphyUtils.applyFontToTextView(this, tvSumCommis, Constants.FONT_REGULAR);
            llMain.addView(tvSumCommis, lParams);
            // TODO inflater
            mCommissionEditText = new EditText(this);
            mCommissionEditText.setTextSize(22);
            mCommissionEditText.setMinEms(6);
            CalligraphyUtils.applyFontToTextView(this, mCommissionEditText, Constants.FONT_REGULAR);
            mCommissionEditText.setKeyListener(new CurrencyKeyListener(CurrencyHelper.ROUBLE_CURRENCY_NUMBER, true));
            mCommissionEditText.addTextChangedListener(new CurrencyFormattingTextWatcher(CurrencyHelper.ROUBLE_CURRENCY_NUMBER, true));
            mCommissionEditText.addTextChangedListener(mCommissionTextWatcherWrapper);
            llMain.addView(mCommissionEditText, lParams2);

            //сумма к выводу
            TextView tvSum = new TextView(this);
            tvSum.setText(R.string.sum_output);
            tvSum.setTextColor(Color.parseColor("#BDBDBD"));
            CalligraphyUtils.applyFontToTextView(this, tvSum, Constants.FONT_REGULAR);
            llMain.addView(tvSum, lParams);
            // TODO inflater
            mAmountEditText = new EditText(this);
            mAmountEditText.setTextSize(22);
            mAmountEditText.setMinEms(6);
            CalligraphyUtils.applyFontToTextView(this, mAmountEditText, Constants.FONT_REGULAR);
            mAmountEditText.setKeyListener(new CurrencyKeyListener(CurrencyHelper.ROUBLE_CURRENCY_NUMBER, true));
            mAmountEditText.addTextChangedListener(new CurrencyFormattingTextWatcher(CurrencyHelper.ROUBLE_CURRENCY_NUMBER, true));
            mAmountEditText.addTextChangedListener(mAmountTextWatcherWrapper);
            llMain.addView(mAmountEditText, lParams2);

            //Вывести
            LinearLayout.LayoutParams lParams3 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lParams3.gravity = Gravity.CENTER_HORIZONTAL;
            lParams3.topMargin = 20;
            TextView tvRemove = new TextView(this);
            tvRemove.setText(getString(R.string.remove));
            tvRemove.setTextSize(24);
            tvRemove.setTextColor(Color.RED);
            CalligraphyUtils.applyFontToTextView(this, tvRemove, Constants.FONT_REGULAR);
            tvRemove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkFields()) {
                        Intent intent = new Intent(WithdrawByTemplateActivity.this, ConfirmWithdrawalActivity.class);
                        intent.putExtra("SumOutput", CurrencyHelper.parseAmount(mAmountEditText.getText()).toPlainString());
                        intent.putExtra("SumCommis", CurrencyHelper.parseAmount(mCommissionEditText.getText()).toPlainString());
                        intent.putExtra("templateId", templateId);
                        intent.putExtra("token", Session.getInstance().getAuthtoken());
                        startActivity(intent);
                        finish();
                    }
                }
            });
            llMain.addView(tvRemove, lParams3);
        }

        Picasso.with(this)
                .load(template.getLogoUrl())
                .into(iconView);


        titleView.setText(template.title);

        //Расписание
        lParams4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //lParams4.gravity = Gravity.CENTER_HORIZONTAL;
        lParams4.topMargin = 20;
        lParams4.leftMargin = 30;
        lParams4.rightMargin = 30;
        lParams4.bottomMargin = 20;
        TextView tvSchedule = new TextView(this);
        tvSchedule.setText(template.schedule == null ? "" :
                template.schedule.getDescription(getResources()));
        tvSchedule.setTextColor(Color.parseColor("#BDBDBD"));
        CalligraphyUtils.applyFontToTextView(this, tvSchedule, Constants.FONT_REGULAR);
        llMain.addView(tvSchedule, lParams4);
        tvSchedule.setGravity(Gravity.CENTER_HORIZONTAL);

    }

    //ответ на запрос Provider
    public void onProviderLoaded(Provider provider) {
        mProvider = provider;

        // XXX inflate в коде. Переделать.
        TextView tvComis = new TextView(this);
        tvComis.setTextColor(Color.parseColor("#BDBDBD"));
        CalligraphyUtils.applyFontToTextView(this, tvComis, Constants.FONT_REGULAR);
        llMain.addView(tvComis, lParams4);
        tvComis.setGravity(Gravity.CENTER_HORIZONTAL);

        CharSequence commissionDescription = TextUtilsW1.formatCommission(provider.commission,
                provider.currencyId, getResources());
        CharSequence amountDescription = TextUtilsW1.formatAmountRange(provider.minAmount,
                provider.maxAmount, provider.currencyId, getResources());
        SpannableStringBuilder ssb = new SpannableStringBuilder(commissionDescription);
        if (ssb.length() != 0) ssb.append(" ");
        ssb.append(amountDescription);

        tvComis.setText(ssb);

        if (!mIsBusinessAccount) {
            // XXX Округление суммы до целого значения прямо в тексте. Жесть, надо переделать.
            if (!mSum.equals("")) {
                int pos = mSum.indexOf(",");
                String sum;
                if (pos > 0) {
                    sum = mSum.substring(0, pos);
                } else {
                    sum = mSum;
                }
                mAmountEditText.setText(sum);
            }
        }
    }

    void startPBAnim() {
        totalReq += 1;
        if (totalReq == 1) {
            progressView.setVisibility(View.VISIBLE);
        }
    }
    
    public void stopPBAnim() {
        totalReq -= 1;
        if (totalReq == 0) {
            progressView.setVisibility(View.INVISIBLE);
        }
    }


    void setAmountText(String summ) {
        String current = mAmountEditText.getText().toString();
        if (!current.equals(summ)) {
            mAmountTextWatcherWrapper.disable();
            mAmountEditText.setText(summ);
            mAmountTextWatcherWrapper.enable();
        }
    }

    void setCommissionText(String commission) {
        String current = mCommissionEditText.getText().toString();
        if (!current.equals(commission)) {
            mCommissionTextWatcherWrapper.disable();
            mCommissionEditText.setText(commission);
            mCommissionTextWatcherWrapper.enable();
        }
    }

    void afterSumChange() {
        if (DBG) Log.d(TAG, "afterSumChange()");
        BigDecimal origInputSum =  CurrencyHelper.parseAmount(mAmountEditText.getText());
        if (origInputSum != null) {

            BigDecimal inputSum = Utils.clamp(origInputSum, mProvider.minAmount, mProvider.maxAmount)
                    .setScale(2, RoundingMode.UP);

            if (origInputSum.compareTo(inputSum) != 0) {
                // Сумма за пределами разрешенных значений
                setCommissionText("");
                if (TextUtils.isEmpty(mCommissionEditText.getHint())) {
                    mCommissionEditText.setHint(origInputSum.compareTo(inputSum) < 0 ? R.string.amount_too_small : R.string.amount_too_large);
                }
            } else {
                setCommissionText(mProvider.getSumWithCommission(inputSum).setScale(2, RoundingMode.UP).toPlainString());
                if (!TextUtils.isEmpty(mCommissionEditText.getHint())) mCommissionEditText.setHint("");
            }
        } else {
            setCommissionText("");
            if (!TextUtils.isEmpty(mCommissionEditText.getHint())) mCommissionEditText.setHint("");
        }
    }
    
    void afterComisChange() {
        BigDecimal commissionVal = CurrencyHelper.parseAmount(mCommissionEditText.getText());
        if (commissionVal != null) {
            // TODO Здесь нужны тесты. Возможны ошибки на пограничных значениях
            BigDecimal inputSum = Utils.clamp(commissionVal,
                    mProvider.getMinAmountWithComission(), mProvider.getMaxAmountWithComission())
                    .setScale(2, RoundingMode.UP);

            if (commissionVal.compareTo(inputSum) != 0) {
                // Комиссия за пределами разрешенных значений
                setAmountText("");
                if (TextUtils.isEmpty(mAmountEditText.getHint())) {
                    mAmountEditText.setHint(commissionVal.compareTo(inputSum) < 0 ? R.string.commission_too_small : R.string.commission_too_large);
                }
            } else {
                inputSum = inputSum.subtract(mProvider.commission.cost);
                BigDecimal rate = mProvider.commission.rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
                BigDecimal sumWOComis = inputSum.divide(BigDecimal.ONE.add(rate), 2, RoundingMode.HALF_EVEN);
                setAmountText(sumWOComis.toPlainString());
                if (!TextUtils.isEmpty(mAmountEditText.getHint())) mAmountEditText.setHint("");
            }
        } else {
            setAmountText("");
            if (!TextUtils.isEmpty(mAmountEditText.getHint())) mAmountEditText.setHint("");
        }
    }
}

