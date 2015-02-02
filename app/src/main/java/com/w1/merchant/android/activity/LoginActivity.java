package com.w1.merchant.android.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.DialogNoInet;
import com.w1.merchant.android.extra.DialogTimeout;
import com.w1.merchant.android.request.JSONParsing;
import com.w1.merchant.android.request.POSTOtp;
import com.w1.merchant.android.request.POSTSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends Activity {

	TextView tvAuth, tvForgot, tvLogoText;
	ImageView ivDelete;
	Intent intent;
	EditText etPassword;
	POSTSession postSession;
	POSTOtp postOtp;
	String[] requestData = { "", "", "", "", "", "" };
	String token, userId;
	public static Typeface w1Rouble;
	public static Typeface tfRobotoLight;
	public static Typeface tfRobotoThin;
	public final String APP_PREF = "W1_Pref";
	SharedPreferences sPref;
	public ProgressBar pbLogin;
	Vibrator v;
	long milliseconds = 100;
	String pattern = "[0-9]{11}";
	AutoCompleteTextView actvLogin;
	Set<String> logins = new HashSet<String>();
	ArrayList<String> loginsArray = new ArrayList<String>();
	DialogFragment dlgNoInet;
	DialogFragment dlgSessionTimeout;
	RelativeLayout rlCenter, rlMain;
	
	private static final int ACT_MENU = 1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login6);
		rlCenter = (RelativeLayout) findViewById(R.id.rlCenter);
		rlMain = (RelativeLayout) findViewById(R.id.rlMain);

		if (!isNetworkConnected()) {
			dlgNoInet = new DialogNoInet();
			dlgNoInet.show(getFragmentManager(), "dlgNoInet");
		}
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		w1Rouble = Typeface.createFromAsset(getAssets(),
				"fonts/W1Rouble-Regular.otf");
		tfRobotoLight = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Light.ttf");
		tfRobotoThin = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Thin.ttf");
		
		sPref = getSharedPreferences(APP_PREF, MODE_PRIVATE);
		
		pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
		tvAuth = (TextView) findViewById(R.id.tvAuth);
		tvLogoText = (TextView) findViewById(R.id.tvLogoText);
		tvLogoText.setTypeface(tfRobotoLight);
		
		
		actvLogin = (AutoCompleteTextView) findViewById(R.id.actvLogin);
		logins = sPref.getStringSet("logins", new HashSet<String>());
		for(String r : logins) {
			loginsArray.add(r);
		}
		actvLogin.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_dropdown_item_1line, loginsArray));
		if ((actvLogin.getText().toString().indexOf("@") > 0) |
				(actvLogin.getText().toString().matches(pattern))) {
			tvAuth.setTextColor(Color.parseColor("#FF0000"));
		}
		
		//значок удалить после логина
		ivDelete = (ImageView)findViewById(R.id.ivDelete);
		if (TextUtils.isEmpty(actvLogin.getText().toString())) {
			ivDelete.setVisibility(View.INVISIBLE);
		} else {
			ivDelete.setVisibility(View.VISIBLE);
		}
		ivDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				actvLogin.setText("");
			}
		});
		actvLogin.addTextChangedListener(twLogin);
		
		//"Забыл" появляется при наведении фокуса
		etPassword = (EditText) findViewById(R.id.etPassword);
		etPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tvForgot.setVisibility(View.VISIBLE);
					rlCenter.setY(rlCenter.getY() - 100);
				} else {
					tvForgot.setVisibility(View.INVISIBLE);
					rlCenter.setY(rlCenter.getY() + 100);
				}
			}
		});
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
		
		//"Забыл"
		tvForgot = (TextView) findViewById(R.id.tvForgot);
		tvForgot.setVisibility(View.INVISIBLE);
		tvForgot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				v.vibrate(milliseconds);
				//запрос капчи
				//requestData[0] = Urls.URL + Urls.URL_CAPTCHA;
//				requestData[1] = "300"; 
//      			requestData[2] = "150";
//      			postCaptcha = new POSTCaptcha(activity);
//    			postCaptcha.execute(requestData);
				
				//одноразовый пароль
				requestData[0] = Constants.URL_OTP;
				requestData[1] = actvLogin.getText().toString(); 
      			postOtp = new POSTOtp(LoginActivity.this);
    			postOtp.execute(requestData);
    		}
		});
		
		tvAuth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

			}
		});

        if (BuildConfig.DEBUG && "debug".equals(BuildConfig.BUILD_TYPE) && !TextUtils.isEmpty(BuildConfig.API_TEST_USER)) {
            // Ибо заебал
            String req[] = new String[] {
                    Constants.URL_SESSION, BuildConfig.API_TEST_USER, BuildConfig.API_TEST_PASS
            };
            postSession = new POSTSession(LoginActivity.this);
            postSession.execute(req);
        }
    }

    private void attemptLogin() {
        if (!actvLogin.getText().toString().contains("@") &&
                !actvLogin.getText().toString().matches(pattern)) {
            return;
        }

        if (!checkFields()) return;
        v.vibrate(milliseconds);

        if (checkFields()) {
            loginsArray.add(actvLogin.getText().toString());
            Set<String> newLogin = new HashSet<String>();
            for (String n : loginsArray) {
                newLogin.add(n);
            }
            SharedPreferences.Editor ed = sPref.edit();
            ed.putStringSet("logins", newLogin);
            ed.apply();
            //создание сессии по логину и паролю
            requestData[0] = Constants.URL_SESSION;
            requestData[1] = actvLogin.getText().toString();
            requestData[2] = etPassword.getText().toString();
            postSession = new POSTSession(LoginActivity.this);
            postSession.execute(requestData);
        }
    }

	//Проверка логина и пароля на пустоту
	public boolean checkFields() {
		boolean result = false;
		
		if (TextUtils.isEmpty(actvLogin.getText().toString())) {
			actvLogin.setError(getString(R.string.error_field));
			result = false;
		} else result = true;
		if (TextUtils.isEmpty(etPassword.getText().toString())) {
			if (result)	etPassword.setError(getString(R.string.error_field));
			result = false;
		} else result = true;
		
		return result;
	}
	
	//Кнопка очистить логин	
	TextWatcher twLogin = new TextWatcher() {
		
		@Override
		public void afterTextChanged(Editable s) {}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			if (TextUtils.isEmpty(arg0)) {
				ivDelete.setVisibility(View.INVISIBLE);
			} else {
				ivDelete.setVisibility(View.VISIBLE);
			}
			if ((arg0.toString().indexOf("@") > 0) | (arg0.toString().matches(pattern))) {
				tvAuth.setTextColor(Color.parseColor("#FF0000"));
			} else {
				tvAuth.setTextColor(Color.parseColor("#E0E0E0"));
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == ACT_MENU) {
			if (resultCode == RESULT_OK) {
				dlgSessionTimeout = new DialogTimeout();
				dlgSessionTimeout.show(getFragmentManager(), "dlgSessionTimeout");

    		}
		}
	}

	public void sessionResult(String[] httpResult) {
		if (httpResult[0].equals("200")) {
			String idData[] = { "", "", "" };
			idData = JSONParsing.session(httpResult[1]);
			token = idData[1];//вместо Bearer после авторизации
			userId = idData[0];
			etPassword.setText("");
			actvLogin.setText("");
			tvForgot.setVisibility(View.INVISIBLE);
            Session.bearer = token;
			
			//запуск основной Activity
			intent = new Intent(this, MenuActivity.class);
			intent.putExtra("token", token);
			intent.putExtra("userId", userId);
			intent.putExtra("timeout", idData[2]);
			startActivityForResult(intent, ACT_MENU);
		} else if (httpResult[0].equals("400")) {
			Toast toast = Toast.makeText(this, getString(R.string.bad_password),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		} else if (httpResult[0].equals("404")) {
			Toast toast = Toast.makeText(this, getString(R.string.user_not_found),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		}
	}

	//Ответ на запрос одноразового пароля
	public void otpResult(String[] httpRes) {
		if (httpRes[0].equals("200")) {
			Toast toast = Toast.makeText(this, getString(R.string.pass_sent,
					actvLogin.getText().toString()), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		}
	}
	
	//Проверка доступа в Инет
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
		   // There are no active networks.
		   return false;
		  } else
		   return true;
	}
}

