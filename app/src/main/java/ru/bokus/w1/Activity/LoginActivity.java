package ru.bokus.w1.Activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.bokus.w1.Constants;
import ru.bokus.w1.Extra.DialogNoInet;
import ru.bokus.w1.Extra.DialogTimeout;
import ru.bokus.w1.Extra.SoftKeyboard;
import ru.bokus.w1.Request.JSONParsing;
import ru.bokus.w1.Request.POSTOtp;
import ru.bokus.w1.Request.POSTPasswordRestore;
import ru.bokus.w1.Request.POSTSession;
import ru.bokus.w1.Request.Urls;

public class LoginActivity extends Activity {

	TextView tvAuth, tvForgot, tvLogoText;
	Button btnReg, btnAuth;
	ImageView ivDelete;
	Intent intent;
	EditText etPassword;
	POSTSession postSession;
	POSTOtp postOtp;
	POSTPasswordRestore postPasswordRestore;
	String[] requestData = { "", "", "", "", "", "" };
	public static HttpClient httpclient = new DefaultHttpClient();
	String[] httpResult = { "", "" };
	String httpResultStr; 
	public Activity activity;
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
	SoftKeyboard softKeyboard;
	
	private static final int ACT_MENU = 1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login6);
		activity = this;
		rlCenter = (RelativeLayout) findViewById(R.id.rlCenter);
		rlMain = (RelativeLayout) findViewById(R.id.rlMain);
		
//		InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
	     
		/*
		Instantiate and pass a callback
		
		softKeyboard = new SoftKeyboard(rlMain, im);
		softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
		{
		  
		    @Override
		    public void onSoftKeyboardHide() 
		    {
		    }
		  
		    @Override
		    public void onSoftKeyboardShow() 
		    {
		    }   
		});*/
		
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
		//etLogin = (EditText) findViewById(R.id.etLogin);
		//etLogin.setText(sPref.getString("login", ""));
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
      			postOtp = new POSTOtp(activity);
    			postOtp.execute(requestData);
    		}
		});
		
		tvAuth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((actvLogin.getText().toString().indexOf("@") > 0) |
						(actvLogin.getText().toString().matches(pattern))) {
					v.vibrate(milliseconds);
					if (checkFields()) {
						loginsArray.add(actvLogin.getText().toString());
						Set<String> newLogin = new HashSet<String>();
						for (String n : loginsArray) {
							newLogin.add(n);
						}
						Editor ed = sPref.edit();
						ed.putStringSet("logins", newLogin);
						ed.apply();
						//создание сессии по логину и паролю
						requestData[0] = Urls.URL + Urls.URL_SESSION;
						
						requestData[1] = actvLogin.getText().toString(); 
	          			requestData[2] = etPassword.getText().toString();
	          			postSession = new POSTSession(activity);
	        			postSession.execute(requestData);
	        		}
				}
			}
		});
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
	
//	//восстановление пароля
//	requestData[0] = Urls.URL + Urls.URL_PASSWORD_RESTORE +
//			etLogin.getText().toString() + "%7D";
//	requestData[1] = ""; 
//		requestData[2] = intent.getStringExtra("captchaId");
//		requestData[3] = intent.getStringExtra("captchaText");
//		postPasswordRestore = new POSTPasswordRestore(activity);
//		postPasswordRestore.execute(requestData);
//					
//		try {
//			httpResult = postPasswordRestore.get();
//			//Log.d("1", "Result pass " + httpResult[0] + " " + httpResult[1]);
//			if (httpResult[0].equals("200")) {
//				
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
	
	/*private void cancelTask() {
	    if (httpGET == null) return;
	    httpGET.cancel(false);
	    Toast.makeText(this, getString(R.string.error_wait), Toast.LENGTH_LONG).show();
	}*/
	
	public void sessionResult(String[] httpResult) {
		if (httpResult[0].equals("200")) {
			String idData[] = { "", "", "" };
			idData = JSONParsing.session(httpResult[1]);
			token = idData[1];//вместо Bearer после авторизации
			userId = idData[0];
			etPassword.setText("");
			actvLogin.setText("");
			tvForgot.setVisibility(View.INVISIBLE);
			
			//запуск основной Activity
			intent = new Intent(activity, MenuActivity.class);
			intent.putExtra("token", token);
			intent.putExtra("userId", userId);
			intent.putExtra("timeout", idData[2]);
			startActivityForResult(intent, ACT_MENU);
		} else if (httpResult[0].equals("400")) {
			Toast toast = Toast.makeText(activity, getString(R.string.bad_password),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		} else if (httpResult[0].equals("404")) {
			Toast toast = Toast.makeText(activity, getString(R.string.user_not_found),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		}
	}
	
//	public void captchaResult(String[] httpRes) {
//		if (httpRes[0].equals("200")) {
//			//вывод капчи пользователю
//			String captchaData[] = { "", "" };
//			captchaData = JSONParsing.captcha(httpRes[1]);
//			intent = new Intent(activity, Captcha.class);
//			intent.putExtra("id", captchaData[0]);
//			intent.putExtra("url", captchaData[1]);
//			startActivityForResult(intent, ACT_CAPT);
//		}
//	}
	
	//Ответ на запрос одноразового пароля
	public void otpResult(String[] httpRes) {
		if (httpRes[0].equals("200")) {
			Toast toast = Toast.makeText(activity, getString(R.string.pass_sent,
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
	
	/* Prevent memory leaks:
	*/
	@Override
	public void onDestroy()
	{
	    super.onDestroy();
	    //softKeyboard.unRegisterSoftKeyboardCallback();
	}
			
}

