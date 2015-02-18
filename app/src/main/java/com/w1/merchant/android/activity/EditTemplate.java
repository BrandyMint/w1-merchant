package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
import com.w1.merchant.android.model.Provider;
import com.w1.merchant.android.model.Template;
import com.w1.merchant.android.service.ApiPayments;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;
import com.w1.merchant.android.viewextended.EditTextRouble;

import java.math.BigDecimal;

import retrofit.Callback;
import retrofit.client.Response;

public class EditTemplate extends Activity {

    private static final String pattern = "[^0-9]";

	private EditTextRouble etSum;
    private EditTextRouble etSumCommis;
    private TextView tvOutputName;
    private ImageView ivOutputIcon;
	private ProgressBar pbTemplates;
    private String templateId;
    private boolean mIsBusinessAccount;
    private LinearLayout llMain;
    private int totalReq = 0;
    private LinearLayout.LayoutParams lParams4;
    private String sum = "";

    private Provider mProvider;

    private ApiPayments mApiPayments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_template);

        mApiPayments = NetworkUtils.getInstance().createRestAdapter().create(ApiPayments.class);

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

    private void loadTemplate() {
        startPBAnim();
        new ApiRequestTask<Template>() {

            @Override
            protected void doRequest(Callback<Template> callback) {
                mApiPayments.getTemplate(templateId, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return EditTemplate.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                stopPBAnim();
                if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "template load error", error);
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(EditTemplate.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(Template template, Response response) {
                stopPBAnim();
                onTemplateLoaded(template);
            }
        }.execute();
    }

    private void loadProvider(final String providerId) {
        startPBAnim();
        new ApiRequestTask<Provider>() {

            @Override
            protected void doRequest(Callback<Provider> callback) {
                mApiPayments.getProvider(providerId, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return EditTemplate.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                stopPBAnim();
                if (BuildConfig.DEBUG) Log.v(Constants.LOG_TAG, "template load error", error);
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(EditTemplate.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(Provider provider, Response response) {
                stopPBAnim();
                onProviderLoaded(provider);
            }
        }.execute();
    }

	boolean checkFields() {
		boolean result;
		
		if (TextUtils.isEmpty(etSumCommis.getText().toString())) {
			etSumCommis.setError(getString(R.string.error_field));
			result = false;
		} else result = true;
		if (TextUtils.isEmpty(etSum.getText().toString())) {
			if (result)	etSum.setError(getString(R.string.error_field));
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
                    sum += field.fieldValue;
                }
            }
        }

		if (!mIsBusinessAccount) {
			//сумма с комиссией
			TextView tvSumCommis = new TextView(this);
			tvSumCommis.setText(getString(R.string.sum_commis));
			tvSumCommis.setTextColor(Color.parseColor("#BDBDBD"));
			llMain.addView(tvSumCommis, lParams);
			etSumCommis = new EditTextRouble(this);
			etSumCommis.setTextSize(22);
			llMain.addView(etSumCommis, lParams2);
			DigitsKeyListener digkl2 = DigitsKeyListener.getInstance();
			etSumCommis.setKeyListener(digkl2);
			//etSumCommis.addTextChangedListener(new ComisTextWatcher());
			etSumCommis.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						afterComisChange();
					}
				}
			});
			
			//сумма к выводу
			TextView tvSum = new TextView(this);
			tvSum.setText(getString(R.string.sum_output));
			tvSum.setTextColor(Color.parseColor("#BDBDBD"));
			llMain.addView(tvSum, lParams);
			etSum = new EditTextRouble(this);
			etSum.setTextSize(22);
			llMain.addView(etSum, lParams2);
			etSum.setKeyListener(digkl2);
			//etSum.addTextChangedListener(new SumTextWatcher());
			etSum.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						afterSumChange();
					}
				}
			});
			
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
						Intent intent = new Intent(EditTemplate.this, ConfirmOutActivity.class);
						intent.putExtra("SumOutput", 
								etSum.getText().toString().replaceAll(pattern, ""));
						intent.putExtra("SumCommis", 
								etSumCommis.getText().toString().replaceAll(pattern, ""));
						intent.putExtra("templateId", templateId);
						intent.putExtra("token", Session.getInstance().getBearer());
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
	    	if (!sum.equals("")) {
		    	etSum.setText(sum.substring(0, sum.indexOf(",")));
				afterSumChange();
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

    void afterSumChange() {
    	String inSum = etSum.getText().toString();
		inSum = inSum.replaceAll(pattern, "");
		if (!inSum.isEmpty()) {
            BigDecimal inputSum = Utils.clamp(new BigDecimal(inSum), mProvider.minAmount, mProvider.maxAmount)
                    .setScale(0, BigDecimal.ROUND_UP);

            etSum.setText(inputSum + " C");
			etSumCommis.setText(mProvider.getSumWithCommission(inputSum).setScale(0, BigDecimal.ROUND_UP) + " C");
		} else {
			etSumCommis.setText("");
		}
	}
    
    void afterComisChange() {
    	String inSumCommis = etSumCommis.getText().toString(); 	
		inSumCommis = inSumCommis.replaceAll(pattern, "");
		if (!inSumCommis.isEmpty()) {
            BigDecimal inputSum = Utils.clamp(new BigDecimal(inSumCommis),
                    mProvider.getMinAmountWithComission(), mProvider.getMaxAmountWithComission())
                    .setScale(0, BigDecimal.ROUND_UP);

            etSumCommis.setText(inputSum + " C");
            inputSum = inputSum.subtract(mProvider.commission.cost);
            BigDecimal rate = mProvider.commission.rate.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
			BigDecimal sumWOComis = inputSum.divide(BigDecimal.ONE.add(rate), 0, BigDecimal.ROUND_HALF_UP);
			etSum.setText(sumWOComis + " C");
		} else {
			etSum.setText("");
		}
    }
}

