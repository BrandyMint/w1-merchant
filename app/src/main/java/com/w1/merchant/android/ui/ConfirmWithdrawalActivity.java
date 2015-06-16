package com.w1.merchant.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.thehayro.internal.Constants;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.InitPaymentStep;
import com.w1.merchant.android.rest.model.InitTemplatePaymentRequest;
import com.w1.merchant.android.rest.model.PaymentForm;
import com.w1.merchant.android.rest.model.PaymentState;
import com.w1.merchant.android.rest.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

public class ConfirmWithdrawalActivity extends ActivityBase {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private String templateId;
    private String paymentId;
    private String sumComis;
    private String sum;
    private int totalReq = 0;
    private ProgressBar pbTemplate;

    private SubmitPaymentFormRequest mLastRequest;

    private Subscription mInitPaymentStepSubscription = Subscriptions.unsubscribed();
    private Subscription mSubmitFormSubscription = Subscriptions.unsubscribed();
    private Subscription mGetPaymentStateSubscription = Subscriptions.unsubscribed();
    private Subscription mSubmitForm2Subscription = Subscriptions.unsubscribed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_withdrawal);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitPaymentStepSubscription.unsubscribe();
        mSubmitFormSubscription.unsubscribe();
        mGetPaymentStateSubscription.unsubscribe();
        mSubmitForm2Subscription.unsubscribe();
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

    private <T> Observable<T> initObservable(Observable<T> observable) {
        return AppObservable.bindActivity(this, observable)
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new  Action0() {
                    @Override
                    public void call() {
                        stopPBAnim();
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        CharSequence errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(ConfirmWithdrawalActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }
                });
    }

    private void initPayment() {
        mInitPaymentStepSubscription.unsubscribe();

        startPBAnim();

        Observable<InitPaymentStep> observable = initObservable(
                RestClient.getApiPayments().initTemplatePayment(new InitTemplatePaymentRequest(templateId)));

        mInitPaymentStepSubscription = observable
                .subscribe(new Observer<InitPaymentStep>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "initPayment error", e);
                    }

                    @Override
                    public void onNext(InitPaymentStep initPaymentStep) {
                        onNextStep(initPaymentStep);
                    }
                });
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

        mSubmitFormSubscription.unsubscribe();

        startPBAnim();

        Observable<InitPaymentStep> observable = initObservable(
                RestClient.getApiPayments().submitPaymentForm(paymentId, form));

        mSubmitFormSubscription = observable
                .subscribe(new Observer<InitPaymentStep>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "submitForm error", e);
                    }

                    @Override
                    public void onNext(InitPaymentStep initPaymentStep) {
                        getPaymentState(String.valueOf(initPaymentStep.paymentId));
                    }
                });
    }

    //ответ на запрос Получение состояния платежа
    private void getPaymentState(final String paymentId) {
        mGetPaymentStateSubscription.unsubscribe();
        startPBAnim();

        Observable<PaymentState> observable = initObservable(
                RestClient.getApiPayments().getPaymentState(paymentId));

        mGetPaymentStateSubscription = observable
                .subscribe(new Subscriber<PaymentState>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "getPaymentState error", e);
                    }

                    @Override
                    public void onNext(PaymentState paymentState) {
                        if (PaymentState.PAYMENT_STATE_UPDATED.equals(paymentState.stateId)
                                || PaymentState.PAYMENT_STATE_PROCESSING.equals(paymentState.stateId)
                                || PaymentState.PAYMENT_STATE_CHECKING.equals(paymentState.stateId)
                                || PaymentState.PAYMENT_STATE_PAYING.equals(paymentState.stateId)
                                ) {
                            // Повторное заполнение формы платежа
                            submitForm2();
                        } else {
                            Toast toast = Toast.makeText(ConfirmWithdrawalActivity.this,
                                    getString(R.string.payment_error), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }
                });
    }

    private void submitForm2() {
        mSubmitForm2Subscription.unsubscribe();
        startPBAnim();

        final SubmitPaymentFormRequest form = new SubmitPaymentFormRequest("$Final", mLastRequest.params);
        Observable<InitPaymentStep> observable = initObservable(
                RestClient.getApiPayments().submitPaymentForm(paymentId, form));

        mSubmitForm2Subscription = observable
                .subscribe(new Observer<InitPaymentStep>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "submitForm2 error", e);
                    }

                    @Override
                    public void onNext(InitPaymentStep initPaymentStep) {
                        Intent intent = new Intent(ConfirmWithdrawalActivity.this, ConfirmPaymentActivity.class);
                        intent.putExtra("sum", sumComis);
                        startActivity(intent);
                        ConfirmWithdrawalActivity.this.finish();
                    }
                });
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

