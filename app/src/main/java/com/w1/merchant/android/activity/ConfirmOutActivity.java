package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.model.InitPaymentStep;
import com.w1.merchant.android.model.InitTemplatePaymentRequest;
import com.w1.merchant.android.model.PaymentForm;
import com.w1.merchant.android.model.PaymentState;
import com.w1.merchant.android.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.service.ApiPayments;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiRequestTaskActivity;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;

public class ConfirmOutActivity extends Activity {

    private String templateId;
    private String paymentId;
    private String sumComis;
    private String sum;
    private int totalReq = 0;
    private ProgressBar pbTemplate;

    private ApiPayments mApiPayments;

    private SubmitPaymentFormRequest mLastRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_out);

        pbTemplate = (ProgressBar) findViewById(R.id.pbTemplate);
        templateId = getIntent().getStringExtra("templateId");
        sumComis = getIntent().getStringExtra("SumCommis");
        sum = getIntent().getStringExtra("SumOutput");

        TextView tvSum = (TextView) findViewById(R.id.tvSum);
        tvSum.setText(TextUtilsW1.formatNumber(new BigInteger(sumComis)));
        TextView tvGo = (TextView) findViewById(R.id.tvGo);
        tvGo.setOnClickListener(myOnClickListener);
        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(myOnClickListener);

        mApiPayments = NetworkUtils.getInstance().createRestAdapter().create(ApiPayments.class);
    }

    private final OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.tvGo):
                    //Инициализация платежа с помощью шаблона
                    initPayment();
                    break;
                case (R.id.ivBack):
                    finish();
                    break;
            }
        }
    };

    private void initPayment() {
        startPBAnim();
        new ApiRequestTask<InitPaymentStep>() {

            @Override
            protected void doRequest(Callback<InitPaymentStep> callback) {
                mApiPayments.initTemplatePayment(new InitTemplatePaymentRequest(templateId), callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return ConfirmOutActivity.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                stopPBAnim();
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(ConfirmOutActivity.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(InitPaymentStep initPaymentStep, Response response) {
                stopPBAnim();
                onNextStep(initPaymentStep);
            }
        }.execute();
    }

    //ответ на запрос Инициализация платежа с помощью шаблона
    public void onNextStep(InitPaymentStep result) {
        //Заполнение формы платежа
        List<SubmitPaymentFormRequest.Param> params = new ArrayList<>(result.form.fields.size());

        for (PaymentForm.Field field: result.form.fields) {
            if (PaymentForm.Field.FIELD_TYPE_SCALAR.equals(field.fieldType)) {
                if ("Amount".equalsIgnoreCase(field.fieldId)) {
                    params.add(new SubmitPaymentFormRequest.Param("Amount", String.valueOf(sum)));
                } else {
                    params.add(new SubmitPaymentFormRequest.Param(field.fieldId, field.defaultValue));
                }
            } else if (PaymentForm.Field.FIELD_TYPE_LIST.equals(field.fieldType)) {
                for (PaymentForm.ListPaymentFormFieldItem listItem: field.items) {
                    if (listItem.isSelected) {
                        params.add(new SubmitPaymentFormRequest.Param(field.fieldId, listItem.value));
                    }
                }
            }
        }

        this.paymentId = String.valueOf(result.paymentId);
        this.mLastRequest = new SubmitPaymentFormRequest(result.form.formId, params);
        submitForm();
    }

    private void submitForm() {
        final SubmitPaymentFormRequest form = mLastRequest;

        startPBAnim();
        new ApiRequestTaskActivity<InitPaymentStep>(this, R.string.payment_not) {

            @Override
            protected void doRequest(Callback<InitPaymentStep> callback) {
                mApiPayments.submitPaymentForm(paymentId, form, callback);
            }

            @Override
            protected void stopAnimation() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(InitPaymentStep initPaymentStep, Response response, Activity activity) {
                stopPBAnim();
                getPaymentState(String.valueOf(initPaymentStep.paymentId));
            }


        }.execute();
    }

    //ответ на запрос Получение состояния платежа
    private void getPaymentState(final String paymentId) {
        startPBAnim();
        new ApiRequestTaskActivity<PaymentState>(this, R.string.payment_not) {

            @Override
            protected void doRequest(Callback<PaymentState> callback) {
                mApiPayments.getPaymentState(paymentId, callback);
            }

            @Override
            protected void stopAnimation() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(PaymentState paymentState, Response response, Activity activity) {
                if (PaymentState.PAYMENT_STATE_UPDATED.equals(paymentState.stateId)
                        || PaymentState.PAYMENT_STATE_PROCESSING.equals(paymentState.stateId)
                        || PaymentState.PAYMENT_STATE_CHECKING.equals(paymentState.stateId)
                        || PaymentState.PAYMENT_STATE_PAYING.equals(paymentState.stateId)
                        ) {
                    // Повторное заполнение формы платежа
                    submitForm2();
                } else {
                    Toast toast = Toast.makeText(activity,
                            getString(R.string.payment_error), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                }
            }

        }.execute();
    }

    private void submitForm2() {
        startPBAnim();

        final SubmitPaymentFormRequest form = new SubmitPaymentFormRequest("$Final", mLastRequest.params);

        new ApiRequestTaskActivity<InitPaymentStep>(this, R.string.payment_error) {

            @Override
            protected void doRequest(Callback<InitPaymentStep> callback) {
                mApiPayments.submitPaymentForm(paymentId, form, callback);
            }

            @Override
            protected void stopAnimation() {
                stopPBAnim();
            }

            @Override
            protected void onSuccess(InitPaymentStep initPaymentStep, Response response, Activity activity) {
                Intent intent = new Intent(ConfirmOutActivity.this, ConfirmPayment.class);
                intent.putExtra("sum", sumComis);
                startActivity(intent);
                ConfirmOutActivity.this.finish();
            }

        }.execute();
    }

    void startPBAnim() {
        totalReq += 1;
        if (totalReq == 1) {
            pbTemplate.setVisibility(View.VISIBLE);
        }
    }

    void stopPBAnim() {
        totalReq -= 1;
        if (totalReq == 0) {
            pbTemplate.setVisibility(View.INVISIBLE);
        }
    }
}

