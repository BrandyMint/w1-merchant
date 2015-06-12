package com.w1.merchant.android.ui.withdraw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.CurrencyLimit;
import com.w1.merchant.android.rest.model.ExchangeRate;
import com.w1.merchant.android.rest.model.InitPaymentRequest;
import com.w1.merchant.android.rest.model.InitPaymentStep;
import com.w1.merchant.android.rest.model.InitTemplatePaymentRequest;
import com.w1.merchant.android.rest.model.PaymentFormField;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.SubmitPaymentFormRequest;
import com.w1.merchant.android.rest.service.ApiPayments;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action0;
import rx.functions.Func5;
import rx.subscriptions.Subscriptions;

public class WithdrawActivity extends AppCompatActivity implements PaymentFormInflater.FormSelectActionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    public static final String RESULT_RESULT_TEXT = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.RESULT_RESULT_TEXT";

    private static final String ARG_PROVIDER_ID = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.ARG_PROVIDER_ID";

    private static final String ARG_TEMPLATE_ID = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.ARG_TEMPLATE_ID";

    private static final String ARG_TEMPLATE_TITLE = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.ARG_TEMPLATE_TITLE";

    private static final String ARG_PROVIDER = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.ARG_PROVIDER";

    private static final String BUNDLE_ARG_PROVIDER = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_PROVIDER";

    private static final String BUNDLE_ARG_BALANCE_LIST = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_BALANCE_LIST";

    private static final String BUNDLE_ARG_EXCHANGE_RATES = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_EXCHANGE_RATES";

    private static final String BUNDLE_ARG_USER_LIMITS = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_USER_LIMITS";

    private static final String BUNDLE_ARG_PAYMENT_STEP = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_PAYMENT_STEP";

    private static final String BUNDLE_ARG_FORM_STATE = "com.w1.merchant.android.ui.withdraw.WithdrawActivity.BUNDLE_ARG_FORM_STATE";

    private static final String VIEW_NAME_HEADER_LOGO = "providerLogo_";

    private static final String VIEW_NAME_HEADER_TITLE = "providerTitle_";

    private Subscription mCreatePaymentSubscription = Subscriptions.unsubscribed();

    private Subscription mNextStepSubscription = Subscriptions.unsubscribed();

    private TextView mTitle;

    private View mProgress;

    private TextView mDescriptionView;

    private TextView mCommissionView;

    private ImageView mLogoView;

    private String mProviderId;

    @Nullable
    private String mTemplateId;

    @Nullable
    private String mTemplateTitle;

    @Nullable
    private Provider mProvider;

    private TextView mSubmitButton;

    private ViewGroup mFormContainer;

    private View mFormTitleViewStub;

    @Nullable
    private InitPaymentStep mCurrentStep;

    private List<Balance> mBalances;

    private List<ExchangeRate> mExchangeRates;

    private List<CurrencyLimit> mUserLimits;

    private PaymentFormInflater mFormInflater;

    public static void startCreateTemplateActivity(Context context,
                                           Provider provider,
                                           int resultCode,
                                           View animateFrom,
                                           View titleView,
                                           View logoView) {
        Intent intent = new Intent(context, WithdrawActivity.class);
        intent.putExtra(ARG_PROVIDER_ID, provider.providerId);
        intent.putExtra(ARG_PROVIDER, provider);
        startActivity(context, intent, provider.providerId, resultCode, animateFrom, titleView, logoView);
    }

    public static void startEditTemplate(Context context, String providerId,
                                     String templateId,
                                     String templateTitle,
                                     int resultCode,
                                     View animateFrom,
                                     View titleView,
                                     View logoView) {
        Intent intent = new Intent(context, WithdrawActivity.class);
        intent.putExtra(ARG_PROVIDER_ID, providerId);
        intent.putExtra(ARG_TEMPLATE_ID, templateId);
        intent.putExtra(ARG_TEMPLATE_TITLE, templateTitle);
        startActivity(context, intent, providerId, resultCode, animateFrom, titleView, logoView);
    }

    private static void startActivity(Context context, Intent intent,
                                      String providerId,
                                      int requestCode,
                                      View animateFrom,
                                      View titleView,
                                      View logoView) {
        if (!(context instanceof Activity)) {
            context.startActivity(intent);
            return;
        }

        Activity activity = (Activity)context;
        ActivityOptionsCompat options;

        if (Build.VERSION.SDK_INT >= 21 && titleView != null && logoView != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    Pair.create(titleView, VIEW_NAME_HEADER_TITLE + providerId),
                    Pair.create(logoView, VIEW_NAME_HEADER_LOGO  + providerId));
        } else if (animateFrom != null) {
            options = ActivityOptionsCompat.makeScaleUpAnimation(
                    animateFrom, 0, 0, animateFrom.getWidth(), animateFrom.getHeight());
        } else {
            options = null;
        }
        ActivityCompat.startActivityForResult(activity, intent, requestCode,
                options == null ? null : options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);

        ((TextView)findViewById(R.id.ab2_title)).setText(R.string.title_activity_withdraw);
        mTitle = ((TextView)findViewById(R.id.ab2_title));
        mProgress = findViewById(R.id.ab2_progress);
        mCommissionView = (TextView)findViewById(R.id.commission);
        mLogoView = (ImageView)findViewById(R.id.logo);
        mDescriptionView = (TextView)findViewById(R.id.description);
        mSubmitButton = (TextView)findViewById(R.id.submit_button);
        mFormContainer =  (ViewGroup)findViewById(R.id.form_container);
        mFormTitleViewStub = findViewById(R.id.template_title_stub);

        setSupportActionBar(toolbar);
        int abOptions =  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP;
        getSupportActionBar().setDisplayOptions(abOptions, abOptions);

        mFormInflater = new PaymentFormInflater(mFormContainer, new PaymentFormInflater.InteractionListener() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public void onSubmitFormClicked() {
                WithdrawActivity.this.onSubmitButtonClicked(null);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitButtonClicked(v);
            }
        });

        mProviderId = getIntent().getStringExtra(ARG_PROVIDER_ID);
        mTemplateId = getIntent().getStringExtra(ARG_TEMPLATE_ID);
        mTemplateTitle = getIntent().getStringExtra(ARG_TEMPLATE_TITLE);
        if (mProviderId == null) throw new IllegalArgumentException("No provider");

        ViewCompat.setTransitionName(mTitle, VIEW_NAME_HEADER_TITLE + mProviderId);
        ViewCompat.setTransitionName(mLogoView, VIEW_NAME_HEADER_LOGO + mProviderId);

        if (savedInstanceState != null) {
            mProvider = (Provider)savedInstanceState.getSerializable(BUNDLE_ARG_PROVIDER);
            mBalances = (List<Balance>)savedInstanceState.getSerializable(BUNDLE_ARG_BALANCE_LIST);
            if (mBalances == null) mBalances = Collections.emptyList();
            mExchangeRates = (List<ExchangeRate>)savedInstanceState.getSerializable(BUNDLE_ARG_EXCHANGE_RATES);
            if (mExchangeRates == null) mExchangeRates = Collections.emptyList();
            mUserLimits = (List<CurrencyLimit>)savedInstanceState.getSerializable(BUNDLE_ARG_USER_LIMITS);
            if (mUserLimits == null) mUserLimits = Collections.emptyList();
            mCurrentStep = savedInstanceState.getParcelable(BUNDLE_ARG_PAYMENT_STEP);
            if (mCurrentStep != null) {
                inflateForm();
                Parcelable formState = savedInstanceState.getParcelable(BUNDLE_ARG_FORM_STATE);
                if (formState != null) mFormInflater.restoreState(formState);
            }
        } else {
            mProvider = (Provider) getIntent().getSerializableExtra(ARG_PROVIDER);
            mBalances = Collections.emptyList();
            mExchangeRates = Collections.emptyList();
            mUserLimits = Collections.emptyList();
            mCurrentStep = null;
        }
        setupProvider();
        if (mCurrentStep == null) reloadData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mProvider != null) outState.putSerializable(BUNDLE_ARG_PROVIDER, mProvider);
        if (!mBalances.isEmpty()) {
            outState.putSerializable(BUNDLE_ARG_BALANCE_LIST, new ArrayList<>(mBalances));
        }
        if (!mExchangeRates.isEmpty()) outState.putSerializable(BUNDLE_ARG_EXCHANGE_RATES, new ArrayList<>(mExchangeRates));
        if (!mUserLimits.isEmpty()) outState.putSerializable(BUNDLE_ARG_USER_LIMITS, new ArrayList<>(mUserLimits));
        if (mCurrentStep != null) {
            outState.putParcelable(BUNDLE_ARG_PAYMENT_STEP, mCurrentStep);
            outState.putParcelable(BUNDLE_ARG_FORM_STATE, mFormInflater.getInstanceState());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCreatePaymentSubscription.unsubscribe();
        mNextStepSubscription.unsubscribe();
    }

    private void setupProvider() {
        if (mProvider == null) return;
        setupDescription();
        setupTitle();
        setupCommissionDescription();
        setupLogo();
    }

    private void setupTitle() {
        mTitle.setText(mProvider.title);
    }

    private void setupLogo() {
        String oldLogoUrl = (String)mLogoView.getTag(R.id.tag_image_view_image_url);
        String newLogoUrl = mProvider.getLogoUrl();
        if (TextUtils.equals(oldLogoUrl, newLogoUrl)) return;

        mLogoView.setTag(R.id.tag_image_view_image_url, newLogoUrl);
        Picasso.with(this)
                .load(newLogoUrl)
                .into(mLogoView);
    }

    private void setupDescription() {
        mDescriptionView.setText(mProvider.description);
    }

    private void setupCommissionDescription() {
        CharSequence commissionDescription = TextUtilsW1.formatCommission(mProvider.commission,
                mProvider.currencyId, getResources());
        CharSequence amountDescription = TextUtilsW1.formatAmountRange(mProvider.minAmount,
                mProvider.maxAmount, mProvider.currencyId, getResources());
        SpannableStringBuilder ssb = new SpannableStringBuilder(commissionDescription);
        if (ssb.length() != 0) ssb.append(" ");
        ssb.append(amountDescription);

        mCommissionView.setText(ssb);
    }

    private void setupLoadingState() {
        boolean isLoading = !mCreatePaymentSubscription.isUnsubscribed();
        mProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mFormContainer.setEnabled(!isLoading);
        mSubmitButton.setEnabled(!isLoading);
        mFormTitleViewStub.setEnabled(!isLoading);
    }

    private void setupSubmitButton() {
        if (mCurrentStep == null) {
            mSubmitButton.setVisibility(View.GONE);
        } else {
            mSubmitButton.setVisibility(View.VISIBLE);
            if (mCurrentStep.stepCount == 1) {
                mSubmitButton.setText(R.string.next_step);
            } else {
                mSubmitButton.setText(getString(R.string.next_step_n_from_m, mCurrentStep.step, mCurrentStep.stepCount));
            }
        }
    }

    private void setupFormTitleButton() {
        if (mCurrentStep == null || !mCurrentStep.form.isFinalStep()) {
            mFormTitleViewStub.setVisibility(View.GONE);
        } else {
            if (mFormTitleViewStub instanceof ViewStub) {
                mFormTitleViewStub = ((ViewStub)mFormTitleViewStub).inflate();
            }
            TextView titleView = (TextView)mFormTitleViewStub.findViewById(R.id.scalar_field_title);
            EditText valueView = (EditText)mFormTitleViewStub.findViewById(R.id.scalar_field_value);
            titleView.setText(R.string.enter_template_name_title);
            valueView.setText(mTemplateTitle);
        }
    }

    void onSubmitButtonClicked(@Nullable View view) {
        // TODO
        if (DBG) Log.v(TAG, "onSubmitButtonClicked()");
        boolean formValid = mFormInflater.validateForm();
        if (!formValid) return;
        Assert.assertNotNull(mCurrentStep);
        if (mCurrentStep.form.isFinalStep()) {
            submitCreateEditTemplate();
        } else {
            submitFormStep();
        }
    }

    void inflateForm() {
        mFormInflater.inflate(mCurrentStep.form);
        setupSubmitButton();
        setupFormTitleButton();
    }

    private void showError(CharSequence defaultErrorDescription, Throwable throwable) {
        CharSequence errText;
        if (throwable instanceof ResponseErrorException) {
            errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
        } else {
            errText = defaultErrorDescription;
        }
        Snackbar.make(mFormContainer, errText, Snackbar.LENGTH_LONG).show();
    }

    private void finishWithError(CharSequence defaultErrorDescription, Throwable throwable) {
        CharSequence errText;
        if (throwable instanceof ResponseErrorException) {
            errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
        } else {
            errText = defaultErrorDescription;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_RESULT_TEXT, errText.toString());
        setResult(Activity.RESULT_CANCELED, resultIntent);
        ActivityCompat.finishAfterTransition(WithdrawActivity.this);
    }

    /**
     * Перезагрузка требуемых данных с сервера
     */
    private void reloadData() {
        if (!mCreatePaymentSubscription.isUnsubscribed()) return;
        if (!mNextStepSubscription.isUnsubscribed()) mNextStepSubscription.unsubscribe();

        ApiPayments apiPayments = RestClient.getApiPayments();

        Observable<Provider> providerObservable;
        if (mProvider != null) {
            providerObservable = Observable.just(mProvider);
        } else {
            providerObservable = apiPayments.getProvider(mProviderId);
        }

        Observable<InitPaymentStep> stepObservable;
        if (mTemplateId == null) {
            stepObservable = apiPayments.initPayment(new InitPaymentRequest(mProviderId));
        } else {
            stepObservable = apiPayments.initTemplatePayment(new InitTemplatePaymentRequest(mTemplateId));
        }

        Observable<List<Balance>> balanceObservable = RestClient.getApiBalance().getBalance();
        Observable<ExchangeRate.ResponseList> ratesObservable = RestClient.getApiExchanges().getRates();
        Observable<List<CurrencyLimit>> limitsObservable = RestClient.getApiLimits().getLimits();

        Observable<LoadDataHolder> s =  Observable.zip(providerObservable, stepObservable, balanceObservable, ratesObservable,
                limitsObservable, sLoadDataZipFunc);


        mCreatePaymentSubscription = AppObservable.bindActivity(this, s)
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(mFinallySetupLoadingState)
                .subscribe(new Observer<LoadDataHolder>() {
                    @Override
                    public void onCompleted() {
                        setupProvider();
                        inflateForm();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (DBG) Log.v(TAG, "Reload data error", throwable);
                        ResponseErrorException ree = (ResponseErrorException) throwable;
                        if (ree.isErrorHttp4xx()) {
                            finishWithError(getText(R.string.network_error), throwable);
                        } else {
                            showError(getText(R.string.network_error), throwable);
                        }
                    }

                    @Override
                    public void onNext(LoadDataHolder data) {
                        mProvider = data.provider;
                        mCurrentStep = data.step;
                        mBalances = data.balances;
                        mExchangeRates = data.exchanges;
                        mUserLimits = data.limits;

                    }
                });
        setupLoadingState();
    }

    private void submitFormStep() {
        if (!mCreatePaymentSubscription.isUnsubscribed()) return;
        if (!mNextStepSubscription.isUnsubscribed()) mNextStepSubscription.unsubscribe();

        SubmitPaymentFormRequest formRequest = mFormInflater.readForm();

        Observable<InitPaymentStep> observable = RestClient.getApiPayments().submitPaymentForm(
                mCurrentStep.paymentId.toString(), formRequest
        );
        mNextStepSubscription = AppObservable.bindActivity(this, observable)
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(mFinallySetupLoadingState)
                .subscribe(new Observer<InitPaymentStep>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (DBG) Log.v(TAG, "Submit form error", throwable);
                        showError(getText(R.string.network_error), throwable);
                    }

                    @Override
                    public void onNext(InitPaymentStep initPaymentStep) {
                        // TODO
                        mCurrentStep = initPaymentStep;
                        inflateForm();
                    }
                });

        setupLoadingState();
    }

    private void submitCreateEditTemplate() {
        if (!mCreatePaymentSubscription.isUnsubscribed()) return;
        if (!mNextStepSubscription.isUnsubscribed()) mNextStepSubscription.unsubscribe();

        String templateName = ((TextView)mFormTitleViewStub.findViewById(R.id.scalar_field_value)).getText().toString();

        SubmitPaymentFormRequest request = SubmitPaymentFormRequest.createTemplateRequest(
                mCurrentStep.paymentId.toString(), mTemplateId, templateName);

        Observable<InitPaymentStep> observable = RestClient.getApiPayments().submitPaymentFormProviderId(
                "SaveTemplate", request);

        mNextStepSubscription = AppObservable.bindActivity(this, observable)
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(mFinallySetupLoadingState)
                .subscribe(new Observer<InitPaymentStep>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        showError(getText(R.string.network_error), throwable);
                    }

                    @Override
                    public void onNext(InitPaymentStep initPaymentStep) {
                        // Шаблон создан
                        boolean isEditTemplate = mTemplateId != null;
                        Intent resultIntent = new Intent();
                        CharSequence resultText = getText(isEditTemplate ? R.string.template_edited_successfully : R.string.template_created_successfully);
                        resultIntent.putExtra(RESULT_RESULT_TEXT, resultText.toString());
                        setResult(Activity.RESULT_OK, resultIntent);
                        ActivityCompat.finishAfterTransition(WithdrawActivity.this);
                    }
                });
        setupLoadingState();
    }

    private final Action0 mFinallySetupLoadingState = new Action0() {
        @Override
        public void call() {
            setupLoadingState();
        }
    };

    @Override
    public void onFormListFieldItemSelected(PaymentFormField field, String selectedValue) {
        mFormInflater.onFormListFieldItemSelected(field, selectedValue);
    }

    private static class LoadDataHolder {
        final Provider provider;
        final InitPaymentStep step;
        final List<Balance> balances;
        final List<ExchangeRate> exchanges;
        final List<CurrencyLimit> limits;

        public LoadDataHolder(Provider provider, InitPaymentStep step, List<Balance> balances, List<ExchangeRate> exchanges,
                              List<CurrencyLimit> limits) {
            this.provider = provider;
            this.step = step;
            this.balances = balances;
            this.exchanges = exchanges;
            this.limits = limits;
        }
    }

    private static final Func5<Provider, InitPaymentStep, List<Balance>, ExchangeRate.ResponseList, List<CurrencyLimit>, LoadDataHolder>
            sLoadDataZipFunc = new Func5<Provider, InitPaymentStep, List<Balance>, ExchangeRate.ResponseList, List<CurrencyLimit>, LoadDataHolder>() {

        @Override
        public LoadDataHolder call(Provider provicer, InitPaymentStep initPaymentStep, List<Balance> balances,
                           ExchangeRate.ResponseList rates, List<CurrencyLimit> currencyLimits) {
            return new LoadDataHolder(provicer, initPaymentStep, balances, rates.items, currencyLimits);
        }
    };

}
