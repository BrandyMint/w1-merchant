package com.w1.merchant.android.ui.withdraw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.w1.merchant.android.rest.model.PaymentFormField;
import com.w1.merchant.android.rest.model.PaymentFormListFieldItem;
import com.w1.merchant.android.rest.model.PaymentState;
import com.w1.merchant.android.rest.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.ui.ActivityBase;
import com.w1.merchant.android.ui.ConfirmDialogFragment;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

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

public class ConfirmWithdrawalActivity extends ActivityBase implements ConfirmDialogFragment.InteractionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private String templateId;
    private String paymentId;
    private String sumComis;
    private String sum;
    private int totalReq = 0;
    private ProgressBar mProgressView;

    private SubmitPaymentFormRequest mLastRequest;

    private Subscription mInitPaymentStepSubscription = Subscriptions.unsubscribed();
    private Subscription mSubmitFormSubscription = Subscriptions.unsubscribed();
    private Subscription mGetPaymentStateSubscription = Subscriptions.unsubscribed();
    private Subscription mSubmitForm2Subscription = Subscriptions.unsubscribed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_withdrawal);

        mProgressView = (ProgressBar) findViewById(R.id.ab2_progress);
        TextView tvSum = (TextView) findViewById(R.id.tvSum);
        TextView tvGo = (TextView) findViewById(R.id.tvGo);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        templateId = getIntent().getStringExtra("templateId");
        sumComis = getIntent().getStringExtra("SumCommis");
        sum = getIntent().getStringExtra("SumOutput");

        setSupportActionBar(toolbar);
        int abOptions =  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP;
        getSupportActionBar().setDisplayOptions(abOptions, abOptions | ActionBar.DISPLAY_SHOW_TITLE);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        tvSum.setText(CurrencyHelper.formatAmount(new BigInteger(sumComis), CurrencyHelper.ROUBLE_CURRENCY_NUMBER));
        tvGo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Инициализация платежа с помощью шаблона
                initPayment();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitPaymentStepSubscription.unsubscribe();
        mSubmitFormSubscription.unsubscribe();
        mGetPaymentStateSubscription.unsubscribe();
        mSubmitForm2Subscription.unsubscribe();
    }

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

        for (PaymentFormField field: result.form.fields) {
            if (PaymentFormField.FIELD_TYPE_SCALAR.equals(field.fieldType)) {
                if ("Amount".equalsIgnoreCase(field.fieldId)) {
                    params.add(new SubmitPaymentFormRequest.Param("Amount", String.valueOf(sum)));
                } else {
                    params.add(new SubmitPaymentFormRequest.Param(field.fieldId, field.defaultValue));
                }
            } else if (PaymentFormField.FIELD_TYPE_LIST.equals(field.fieldType)) {
                for (PaymentFormListFieldItem listItem: field.items) {
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
                        String amount = sumComis + '\u00a0' + CurrencyHelper.ROUBLE_SYMBOL;
                        String description = getString(R.string.transact_proces, amount);
                        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(description);
                        FragmentManager fm = getSupportFragmentManager();
                        Fragment old = fm.findFragmentByTag("CONFIRM_DIALOG_FRAGMENT");
                        if (old != null) return;
                        dialog.show(fm, "CONFIRM_DIALOG_FRAGMENT");
                    }
                });
    }

    void startPBAnim() {
        totalReq += 1;
        if (totalReq == 1) {
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    void stopPBAnim() {
        totalReq -= 1;
        if (totalReq == 0) {
            mProgressView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onConfirmDialogDismissed() {
        finish();
        overridePendingTransition(0, 0);
    }
}

