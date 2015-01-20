package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.request.GETProvider;
import com.w1.merchant.android.request.GETTemplate;
import com.w1.merchant.android.request.JSONParsing;
import com.w1.merchant.android.viewextended.EditTextRouble;

import java.util.ArrayList;

public class EditTemplate extends Activity {

	EditTextRouble etSum, etSumCommis;
	TextView tvRemove, tvCardNumber, tvOutputName;
	ImageView ivBack, ivOutputIcon;
	public ProgressBar pbTemplates;
	String[] requestData = { "", "", "", "" };
	String[] provider = { "", "", "", "", "", "", "" };
    GETTemplate getTemplate;
    GETProvider getProvider;
    String httpResultStr, token, title, templateId;
    boolean accountTypeId;
    Intent intent;
    ArrayList<String[]> dataFields = new ArrayList<String[]>();
    LinearLayout llMain;
    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
    int matchParent = LinearLayout.LayoutParams.MATCH_PARENT;
    Context context;
    int totalReq = 0;
    LinearLayout.LayoutParams lParams4;
    String pattern = "[^0-9]";
    String sum = "";
    // Минимальная допустимая сумма платежа
    float minAmount = 0;
    // Максимальная допустимая сумма платежа
    float maxAmount = 0;
    // Надбавка к комиссии 
    float cost = 0;
    // Максимальная комиссия
    float maxComis = 0;
    float bonusRate = 0;
    // Минимальная комиссия
    float minComis = 0;
    // Процент комиссии
    float rate = 0;
    // Минимальная сумма с комиссией
    float minSumWithComis = 0;
    // Максимальная сумма с комиссией
    float maxSumWithComis = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.template_free);
		
		llMain = (LinearLayout) findViewById(R.id.llMain);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(myOnClickListener);
		
		ivOutputIcon = (ImageView) findViewById(R.id.ivOutputIcon);
		context = this;
		
		tvOutputName = (TextView) findViewById(R.id.tvOutputName);
		
		pbTemplates = (ProgressBar) findViewById(R.id.pbTemplates);
		intent = getIntent();
		token = intent.getStringExtra("token");
		templateId = intent.getStringExtra("templateId");
		accountTypeId = intent.getBooleanExtra("accountTypeId", false);
		
		//запрос шаблона
		startPBAnim();
        requestData[0] = Constants.URL_TEMPLATES + intent.getStringExtra("templateId");
        requestData[1] = token;
        getTemplate = new GETTemplate(this);
        getTemplate.execute(requestData);
	}	
	
	public boolean checkFields() {
		boolean result = true;
		
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
	
	OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	//ответ на запрос Template
	public void httpResult(String result) {
		//запрос провайдера
		startPBAnim();
		requestData[0] = Constants.URL_PROVIDERS + JSONParsing.templateField(result, "ProviderId");
        requestData[1] = token;
        getProvider = new GETProvider(this);
        getProvider.execute(requestData);
		
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
		          wrapContent, wrapContent);
		lParams.gravity = Gravity.LEFT;
		lParams.topMargin = 40;
		lParams.leftMargin = 30;
		lParams.rightMargin = 30;
		      
		LinearLayout.LayoutParams lParams2 = new LinearLayout.LayoutParams(
		          wrapContent, wrapContent);
		lParams2.gravity = Gravity.LEFT;
		lParams2.leftMargin = 30;
		lParams2.rightMargin = 30;
			  
		dataFields = JSONParsing.templateId(result);
		for (int i = 0; i < dataFields.size(); i++) {
			if (!dataFields.get(i)[1].startsWith("Сумма")) {
				TextView tvNew = new TextView(this);
				tvNew.setText(dataFields.get(i)[1]);
				tvNew.setTextColor(Color.parseColor("#BDBDBD"));
				llMain.addView(tvNew, lParams);
				TextView tvNew2 = new TextView(this);
				tvNew2.setText(dataFields.get(i)[2]);
				tvNew2.setTextSize(22);
				llMain.addView(tvNew2, lParams2);
			} else {
				sum = dataFields.get(i)[2];
			}
		}
		
		if (!accountTypeId) {
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
			          wrapContent, wrapContent);
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
						intent = new Intent(context, ConfirmOutActivity.class);
						intent.putExtra("SumOutput", 
								etSum.getText().toString().replaceAll(pattern, ""));
						intent.putExtra("SumCommis", 
								etSumCommis.getText().toString().replaceAll(pattern, ""));
						intent.putExtra("templateId", templateId);
						intent.putExtra("token", token);
						startActivity(intent);
						finish();
					}
				}
			});
		    llMain.addView(tvRemove, lParams3);
		}
		
		Picasso.with(context)
				.load(JSONParsing.templateField(result, "ProviderLogoUrl"))
				.into(ivOutputIcon);
		title = JSONParsing.templateTitle(result, "\"Title\":\"");
		if (title.length() < 23) {
			tvOutputName.setText(title);
		} else {
			char[] buffer = new char[22];
			title.getChars(0, 21, buffer, 0);
			tvOutputName.setText(new String(buffer) + "...");
		}
		
		//Расписание
		lParams4 = new LinearLayout.LayoutParams(
		          matchParent, wrapContent);
		//lParams4.gravity = Gravity.CENTER_HORIZONTAL;
		lParams4.topMargin = 20;
		lParams4.leftMargin = 30;
		lParams4.rightMargin = 30;
		lParams4.bottomMargin = 20;
		TextView tvSchedule = new TextView(this);
	    tvSchedule.setText(JSONParsing.templateSchedule(result));
	    tvSchedule.setTextColor(Color.parseColor("#BDBDBD"));
	    llMain.addView(tvSchedule, lParams4);
	    tvSchedule.setGravity(Gravity.CENTER_HORIZONTAL);
	    
	}
	
	//ответ на запрос Provider
	public void providerResult(String result) {
		com.w1.merchant.android.viewextended.TextViewRouble tvComis =
				new com.w1.merchant.android.viewextended.TextViewRouble(this);
		provider = JSONParsing.templateProvider(result);
		if (provider[6].equals("0")) {
			tvComis.setText(getString(R.string.wo_comis, 
					JSONParsing.formatNumber(provider[0]) + " " + "C",
					JSONParsing.formatNumber(provider[1]) + " " + "C"));
		} else {
			tvComis.setText(getString(R.string.comis_sum, 
					provider[6] + "% + " + (provider[2].equals("0") ? "" : 
						JSONParsing.formatNumber(provider[2]) + " " + "C"), 
					JSONParsing.formatNumber(provider[0]) + " " + "C",
					JSONParsing.formatNumber(provider[1]) + " " + "C"));
		}
	    tvComis.setTextColor(Color.parseColor("#BDBDBD"));
	    llMain.addView(tvComis, lParams4);
	    tvComis.setGravity(Gravity.CENTER_HORIZONTAL);
	    
	    
	    
	    // Минимальная допустимая сумма платежа
	    minAmount = Float.parseFloat(provider[0]);
	    // Максимальная допустимая сумма платежа
	    maxAmount = Float.parseFloat(provider[1]);
	    // Надбавка к комиссии 
	    cost = Float.parseFloat(provider[2]);
	    // Максимальная комиссия
	    maxComis = Float.parseFloat(provider[3]);
	    
	    bonusRate = Float.parseFloat(provider[4]);
	    // Минимальная комиссия
	    minComis = Float.parseFloat(provider[5]);
	    // Процент комиссии
	    rate = Float.parseFloat(provider[6]);
	    minSumWithComis = minAmount + getComis(minAmount);
	    maxSumWithComis = maxAmount + getComis(maxAmount);
	    
	    if (!accountTypeId) {    
	    	if (!sum.equals("")) {
		    	etSum.setText(sum.substring(0, sum.indexOf(",")));
				afterSumChange();
			}
	    }
	}
	
	public void startPBAnim() {
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
    
//    public class SumTextWatcher implements TextWatcher {
//	    boolean mEditing;
//	    
//	    public SumTextWatcher() {
//	    	mEditing = false;
//	    }
//	    
//	    public synchronized void afterTextChanged(Editable s) {
//	        if (!mEditing) {
//	            mEditing = true;
//	            s.replace(0, s.length(), s.toString().replaceAll(pattern, ""));
//	            if (!s.toString().isEmpty()) {
//	            	float inputSum = Float.parseFloat(s.toString());
//					if (inputSum < minAmount) {
//						s.replace(0, s.length(), minAmount + "");
//					} else if (inputSum > maxAmount) {
//						s.replace(0, s.length(), maxAmount + "");
//					}
//					
//					inputSum = Float.parseFloat(s.toString());
//					s.replace(0, s.length(), s.toString() + " C");
//					float sumComis = inputSum + getComis(inputSum);
//					etSumCommis.setText((int)sumComis + " C");
//	            }
//	            mEditing = false;
//	        }
//	    }
//	    
//	    public void beforeTextChanged(CharSequence s, int start, int count,
//	    		int after) {
//	    }
//	    
//	    public void onTextChanged(CharSequence s, int start, int before, int count) {
//	    }
//	}
    
//    public class ComisTextWatcher implements TextWatcher {
//	    boolean mEditing;
//	    
//	    public ComisTextWatcher() {
//	    	mEditing = false;
//	    }
//	    
//	    public synchronized void afterTextChanged(Editable s) {
//	        if (!mEditing) {
//	            mEditing = true;
//	            s.replace(0, s.length(), s.toString().replaceAll(pattern, ""));
//	            if (!s.toString().isEmpty()) {
//	            	float inputSum = Float.parseFloat(s.toString());
//					if (inputSum < minSumWithComis) {
//						s.replace(0, s.length(), minSumWithComis + "");
//					} else if (inputSum > maxSumWithComis) {
//						s.replace(0, s.length(), maxSumWithComis + "");
//					}
//					
//					inputSum = Float.parseFloat(s.toString()) - cost;
//					s.replace(0, s.length(), s.toString() + " C");
//					float sumWOComis = inputSum / (1 + rate / 100);
//					//etSum.removeTextChangedListener(twSum);
//					etSum.setText((int) sumWOComis + " C");
//					//etSum.addTextChangedListener(twSum);
//				}
//	            mEditing = false;
//	        }
//	    }
//	    
//	    public void beforeTextChanged(CharSequence s, int start, int count,
//	    		int after) {
//	    }
//	    
//	    public void onTextChanged(CharSequence s, int start, int before, int count) {
//	    }
//	}
    
    public float getComis(float inSum) {
    	float comis = (float) Math.ceil(inSum * rate / 100) +
				cost;
		if (minComis > 0) {
			if (comis < minComis) {
				comis = minComis;
			}
		}
		if (maxComis > 0) {
			if (comis > maxComis) {
				comis = maxComis;
			}
		}
		return comis;
    }
    
    public void afterSumChange() {
    	String inSum = etSum.getText().toString();
		inSum = inSum.replaceAll(pattern, "");
		if (!inSum.isEmpty()) {
			float inputSum = Float.parseFloat(inSum);
			if (inputSum < minAmount) {
				etSum.setText((int)minAmount + " C");
				inputSum = minAmount;
			} else if (inputSum > maxAmount) {
				etSum.setText((int)maxAmount + " C");
				inputSum = maxAmount;
			}  else {
				etSum.setText((int)inputSum + " C");
			}
			
			float sumComis = inputSum + getComis(inputSum);
			etSumCommis.setText((int)sumComis + " C");
		} else {
			etSumCommis.setText("");
		}
	}
    
    public void afterComisChange() {
    	String inSumCommis = etSumCommis.getText().toString(); 	
		inSumCommis = inSumCommis.replaceAll(pattern, "");
		if (!inSumCommis.isEmpty()) {
        	float inputSum = Float.parseFloat(inSumCommis);
			if (inputSum < minSumWithComis) {
				etSumCommis.setText((int)minSumWithComis + " C");
				inputSum = minSumWithComis;
			} else if (inputSum > maxSumWithComis) {
				etSumCommis.setText((int)maxSumWithComis + " C");
				inputSum = maxSumWithComis;
			} else {
				etSumCommis.setText((int)inputSum + " C");
			}
			
			inputSum -= cost;
			float sumWOComis = inputSum / (1 + rate / 100);
			etSum.setText((int) sumWOComis + " C");
		} else {
			etSum.setText("");
		}
    }
}

