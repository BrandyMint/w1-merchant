package ru.bokus.w1.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.bokus.w1.Constants;
import ru.bokus.w1.Request.GETPaymentsState;
import ru.bokus.w1.Request.JSONParsing;
import ru.bokus.w1.Request.POSTPayments;
import ru.bokus.w1.Request.PUT1;
import ru.bokus.w1.Request.PUT2;

public class ConfirmOutActivity extends Activity {

	TextView tvSum, tvGo;
	ImageView ivBack;
	Intent intent;
	String templateId, token, paymentId, sumComis, sum;
	String[] requestData = { "", "", "", "", "", "" };
	POSTPayments postPayments;
	PUT1 putPayments;
	PUT2 putPayments2;
	GETPaymentsState getPaymentsState;
	int totalReq = 0;
	ProgressBar pbTemplate;
	Activity activity;
	ArrayList<String[]> dataPayments;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.confirm_out);
		activity = this;
		
		pbTemplate = (ProgressBar) findViewById(R.id.pbTemplate);
		intent = getIntent();
		tvSum = (TextView) findViewById(R.id.tvSum);
		tvSum.setText(JSONParsing.formatNumber(intent.getStringExtra("SumCommis")));
		tvGo = (TextView) findViewById(R.id.tvGo);
		tvGo.setOnClickListener(myOnClickListener);
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(myOnClickListener);
		templateId = intent.getStringExtra("templateId");
		token = intent.getStringExtra("token");
		sumComis = intent.getStringExtra("SumCommis");
		sum = intent.getStringExtra("SumOutput");
	}	
	
	OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case (R.id.tvGo):
					//Инициализация платежа с помощью шаблона
					requestData[0] = Constants.URL_PAYMENTS;
					requestData[1] = token; 
					requestData[2] = templateId;
					postPayments = new POSTPayments(activity);
					startPBAnim();
					postPayments.execute(requestData);
					//finish();
				break;
				case (R.id.ivBack):
					finish();
				break;
			}
		}
	};
	
	//ответ на запрос Инициализация платежа с помощью шаблона
	public void paymentsResult(String[] result) {
		stopPBAnim();
		if (result[0].equals("201")) {
			//Заполнение формы платежа
			dataPayments = JSONParsing.payments(result[1]);
			for (int i = 0; i < dataPayments.size(); i++) {
				if (dataPayments.get(i)[0].equals("Amount")) {
					String[] elementPayments = { "Amount", sum };
					dataPayments.set(i, elementPayments);
				}
			}
			//test
			//String[] elementPayments = { "ToTitle", "Иванов И.И." };
			//dataPayments.add(elementPayments);
			//String[] elementPayment = { "ToTaxNumber", "212112345678" };
			//dataPayments.add(elementPayment);
			paymentId = dataPayments.get(0)[1];
			requestData[0] = Constants.URL_PAYMENTS + "/" + paymentId;
			requestData[1] = token; 
			putPayments = new PUT1(activity);
			startPBAnim();
			putPayments.execute(requestData);
		}
		//Log.d("1", "template " + JSONParsing.payments(result[1]));
		//Log.d("1", "template " + result[1]);
	}
	
	//ответ на запрос Заполнение формы платежа
	public void putResult(String[] result) {
		stopPBAnim();
		if (result[0].equals("200")) {
			requestData[0] = String.format(Constants.URL_PAYMENT_STATE, paymentId);
	        requestData[1] = token;
	        requestData[2] = "";
	        GETPaymentsState getPaymentsState;
			getPaymentsState = new GETPaymentsState(activity);
			getPaymentsState.execute(requestData);
		} else if (result[0].equals("404")) {
			Toast toast = Toast.makeText(activity, 
					getString(R.string.payment_not), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, 50);
	    	toast.show();
		}
		//Log.d("1", "put " + result[0] + " " + result[1]);
	}
	
	//ответ на запрос Получение состояния платежа
	public void stateResult(String[] result) {
		stopPBAnim();
		if (result[0].equals("200")) {
			String state = JSONParsing.paymentsState(result[1]);
			//Log.d("1", "state " + JSONParsing.paymentsState(result[1]));
			if (state.equals("Updated") | state.equals("Processing") |
					state.equals("Checking") | state.equals("Paying")) {
				//Повторное заполнение формы платежа
				paymentId = dataPayments.get(0)[1];
				requestData[0] = Constants.URL_PAYMENTS + paymentId;
				requestData[1] = token; 
				
				String[] elementPayments = { "FormId", "$Final" };
				dataPayments.set(1, elementPayments);
				putPayments2 = new PUT2(activity);
				startPBAnim();
				putPayments2.execute(requestData);
			} else {
				Toast toast = Toast.makeText(activity, 
						getString(R.string.payment_error), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP, 0, 50);
		    	toast.show();
			}
		}
	}
	
	//ответ на повторный запрос Заполнение формы платежа
		public void putResult2(String[] result) {
			stopPBAnim();
			if (result[0].equals("200")) {
				intent = new Intent(activity, ConfirmPayment.class);
				intent.putExtra("sum", sumComis);
				startActivity(intent);
				finish();
			} else {
				Toast toast = Toast.makeText(activity, 
						getString(R.string.payment_error), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP, 0, 50);
		    	toast.show();
		    	finish();
			}
			//Log.d("1", "put " + result[0] + " " + result[1]);
		}
	
	public void startPBAnim() {
    	totalReq += 1;
    	if (totalReq == 1) {
    		pbTemplate.setVisibility(View.VISIBLE);
    	}
    }
    
    public void stopPBAnim() {
    	totalReq -= 1;
    	if (totalReq == 0) {
    		pbTemplate.setVisibility(View.INVISIBLE);
    	}
    }
    
    public ArrayList<String[]> getDataPayment() {
		return dataPayments;
	}
}

