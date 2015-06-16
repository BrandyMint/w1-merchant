package com.w1.merchant.android.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.Template;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;
import com.w1.merchant.android.ui.widget.EditTextRouble;

import java.math.BigDecimal;
import java.math.RoundingMode;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class WithdrawByTemplateActivity extends ActivityBase {

    private static final String pattern = "[^0-9]";

	private EditTextRouble mAmountEditText;
    private EditTextRouble mCommissionEditText;
    private TextView tvOutputName;
    private ImageView ivOutputIcon;
	private ProgressBar pbTemplates;
    private String templateId;
    private boolean mIsBusinessAccount;
    private LinearLayout llMain;
    private int totalReq = 0;
    private LinearLayout.LayoutParams lParams4;
    private String mSum = "";

    private Provider mProvider;

    private boolean mAmountManualChange;
    private boolean mCommissionManualChange;

    private Subscription mGetProviderSubscription = Subscriptions.empty();
    private Subscription mGetTemplateSubscription = Subscriptions.empty();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_template);

		llMain = (LinearLayout) findViewById(R.id.llMain);
        findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
		
		ivOutputIcon = (ImageView) findViewById(R.id.ivOutputIcon);
		tvOutputName = (TextView) findViewById(R.id.tvOutputName);
		
		pbTemplates = (ProgressBar) findViewById(R.id.pbTemplates);
		templateId = getIntent().getStringExtra("templateId");
		mIsBusinessAccount =  getIntent().getBooleanExtra("mIsBusinessAccount", false);

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

    private String readAmountWithComissionEditText() {
        return mCommissionEditText.getText().toString().replaceAll(pattern, "");
    }

    private String readAmountEditText() {
        return mAmountEditText.getText().toString().replaceAll(pattern, "");
    }

	boolean checkFields() {
		boolean result;
		
		if (TextUtils.isEmpty(readAmountWithComissionEditText())) {
			mCommissionEditText.setError(getString(R.string.error_field));
			result = false;
		} else result = true;
		if (TextUtils.isEmpty(readAmountEditText())) {
			if (result)	mAmountEditText.setError(getString(R.string.error_field));
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
                    llMain.addView(tvNew, lParams);
                    TextView tvNew2 = new TextView(this);
                    tvNew2.setText(field.fieldValue);
                    tvNew2.setTextSize(22);
                    llMain.addView(tvNew2, lParams2);
                } else {
                    mSum += field.fieldValue;
                }
            }
        }

		if (!mIsBusinessAccount) {
			//сумма с комиссией
			TextView tvSumCommis = new TextView(this);
			tvSumCommis.setText(getString(R.string.sum_commis));
			tvSumCommis.setTextColor(Color.parseColor("#BDBDBD"));
			llMain.addView(tvSumCommis, lParams);
			mCommissionEditText = new EditTextRouble(this);
			mCommissionEditText.setTextSize(22);
            mCommissionEditText.setMinEms(6);
			DigitsKeyListener digkl2 = DigitsKeyListener.getInstance();
			mCommissionEditText.setKeyListener(digkl2);
            mCommissionEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mCommissionManualChange) {
                        mCommissionManualChange = false;
                    } else {
                        afterComisChange();
                    }
                }
            });
            llMain.addView(mCommissionEditText, lParams2);
			
			//сумма к выводу
			TextView tvSum = new TextView(this);
			tvSum.setText(getString(R.string.sum_output));
			tvSum.setTextColor(Color.parseColor("#BDBDBD"));
			llMain.addView(tvSum, lParams);
			mAmountEditText = new EditTextRouble(this);
			mAmountEditText.setTextSize(22);
            mAmountEditText.setMinEms(6);
			mAmountEditText.setKeyListener(digkl2);
            mAmountEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (mAmountManualChange) {
                        mAmountManualChange = false;
                    } else {
                        afterSumChange();
                    }
                }
            });
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
		    tvRemove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (checkFields()) {
						Intent intent = new Intent(WithdrawByTemplateActivity.this, ConfirmWithdrawalActivity.class);
						intent.putExtra("SumOutput", readAmountEditText());
						intent.putExtra("SumCommis", readAmountWithComissionEditText());
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
				.into(ivOutputIcon);


        tvOutputName.setText(template.title);
		
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
	    llMain.addView(tvSchedule, lParams4);
	    tvSchedule.setGravity(Gravity.CENTER_HORIZONTAL);
	    
	}
	
	//ответ на запрос Provider
	public void onProviderLoaded(Provider provider) {
        mProvider = provider;

		TextView tvComis = new TextView(this);

        SpannableStringBuilder from = new SpannableStringBuilder(TextUtilsW1.formatNumber(provider.minAmount));
        from.append('\u00a0');
        from.append(TextUtilsW1.getRoubleSymbol(2));

        SpannableStringBuilder to = new SpannableStringBuilder(TextUtilsW1.formatNumber(provider.maxAmount));
        to.append('\u00a0');
        to.append(TextUtilsW1.getRoubleSymbol(2));

        if (BigDecimal.ZERO.compareTo(provider.commission.rate) == 0) {
            String pattern = getString(R.string.wo_comis);
            SpannableStringBuilder sb = new SpannableStringBuilder(pattern);

            int fromPos = pattern.indexOf("$from");
            sb.replace(fromPos, fromPos + "$from".length(), from);

            int toPos = sb.toString().indexOf("$to");
            sb.replace(toPos, toPos + "$to".length(), to, 0, to.length());

			tvComis.setText(sb);
		} else {
            String pattern = getString(R.string.comis_sum);
            SpannableStringBuilder commission = new SpannableStringBuilder(String.valueOf(provider.commission.rate));
            commission.append("%");
            if (BigDecimal.ZERO.compareTo(provider.commission.cost) != 0) {
                commission.append("\u00a0+\u00a0");
                commission.append(TextUtilsW1.formatNumber(provider.commission.cost));
                commission.append('\u00a0');
                commission.append(TextUtilsW1.getRoubleSymbol(2));
            }

            SpannableStringBuilder sb = new SpannableStringBuilder(pattern);
            int commissionPos = pattern.indexOf("$commission");
            sb.replace(commissionPos, commissionPos + "$commission".length(), commission);

            int fromPos = sb.toString().indexOf("$from");
            sb.replace(fromPos, fromPos + "$from".length(), from);

            int toPos = sb.toString().indexOf("$to");
            sb.replace(toPos, toPos + "$to".length(), to);

            tvComis.setText(sb);
		}
	    tvComis.setTextColor(Color.parseColor("#BDBDBD"));
	    llMain.addView(tvComis, lParams4);
	    tvComis.setGravity(Gravity.CENTER_HORIZONTAL);

        if (!mIsBusinessAccount) {
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
    		pbTemplates.setVisibility(View.VISIBLE);
    	}
    }
    
    public void stopPBAnim() {
    	totalReq -= 1;
    	if (totalReq == 0) {
    		pbTemplates.setVisibility(View.INVISIBLE);
    	}
    }


    void setAmountText(String summ) {
        String current = mAmountEditText.getText().toString();
        if (!current.equals(summ)) {
            mAmountManualChange = true;
            mAmountEditText.setText(summ);
        }
    }

    void setCommissionText(String commission) {
        String current = mCommissionEditText.getText().toString();
        if (!current.equals(commission)) {
            mCommissionManualChange = true;
            mCommissionEditText.setText(commission);
        }
    }

    void afterSumChange() {
    	String inSumStringOrig = mAmountEditText.getText().toString();
		String inSum = inSumStringOrig.replaceAll(pattern, "");
		if (!inSum.isEmpty()) {
            BigDecimal origInputSum =  new BigDecimal(inSum);
            BigDecimal inputSum = Utils.clamp(origInputSum, mProvider.minAmount, mProvider.maxAmount)
                    .setScale(0, RoundingMode.UP);

            if (origInputSum.compareTo(inputSum) != 0) {
                // Сумма за пределами разрешенных значений
                setCommissionText("");
                if (TextUtils.isEmpty(mCommissionEditText.getHint())) {
                    mCommissionEditText.setHint(origInputSum.compareTo(inputSum) < 0 ? R.string.amount_too_small : R.string.amount_too_large);
                }
            } else {
                setCommissionText(mProvider.getSumWithCommission(inputSum).setScale(0, RoundingMode.UP) + " C");
                if (!TextUtils.isEmpty(mCommissionEditText.getHint())) mCommissionEditText.setHint("");
            }

            if (inSumStringOrig.indexOf("C") != inSumStringOrig.length() - 1) {
                setAmountText(origInputSum + " C");
            }
		} else {
            setCommissionText("");
            if (!TextUtils.isEmpty(mCommissionEditText.getHint())) mCommissionEditText.setHint("");
		}
	}
    
    void afterComisChange() {
    	String commissionTextOrig = mCommissionEditText.getText().toString();
		String commissionTextNum = commissionTextOrig.replaceAll(pattern, "");
		if (!commissionTextNum.isEmpty()) {
            BigDecimal commissionVal = new BigDecimal(commissionTextNum);
            // TODO Здесь нужны тесты. Возможны ошибки на пограничных значениях
            BigDecimal inputSum = Utils.clamp(commissionVal,
                    mProvider.getMinAmountWithComission(), mProvider.getMaxAmountWithComission())
                    .setScale(0, RoundingMode.UP);

            if (commissionVal.compareTo(inputSum) != 0) {
                // Комиссия за пределами разрешенных значений
                setAmountText("");
                if (TextUtils.isEmpty(mAmountEditText.getHint())) {
                    mAmountEditText.setHint(commissionVal.compareTo(inputSum) < 0 ? R.string.commission_too_small : R.string.commission_too_large);
                }
            } else {
                inputSum = inputSum.subtract(mProvider.commission.cost);
                BigDecimal rate = mProvider.commission.rate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
                BigDecimal sumWOComis = inputSum.divide(BigDecimal.ONE.add(rate), 0, RoundingMode.DOWN);
                setAmountText(sumWOComis + " C");
                if (!TextUtils.isEmpty(mAmountEditText.getHint())) mAmountEditText.setHint("");
            }

            if (commissionTextOrig.indexOf("C") != commissionTextOrig.length() - 1) {
                setCommissionText(commissionVal + " C");
            }

		} else {
            setAmountText("");
            if (!TextUtils.isEmpty(mAmountEditText.getHint())) mAmountEditText.setHint("");
		}
    }
}

