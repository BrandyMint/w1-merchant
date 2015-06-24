package com.w1.merchant.android.ui.exchange;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.ExchangeRate;
import com.w1.merchant.android.rest.model.ExchangeRateStatus;
import com.w1.merchant.android.rest.model.ExchangeRequest;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by alexey on 13.07.15.
 */
public class ExchangeDialogFragment extends DialogFragment {

    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "ExchangeDialogFragment";

    private static final String ARG_SRC_CURRENCY = "ARG_SRC_CURRENCY";

    private static final String ARG_DST_CURRENCY = "ARG_DST_CURRENCY";

    private static final String ARG_AMOUNT = "ARG_AMOUNT";

    private static final String ARG_AMOUNT_IS_TARGET = "ARG_AMOUNT_IS_TARGET";

    private static final String ARG_EXCHANGE_RATE = "ARG_EXCHANGE_RATE";

    private View mProgressView;

    private View mConfirmButton;

    private NoticeDialogListener mListener;

    private Subscription mExchangeSubscription = Subscriptions.unsubscribed();

    private String mSrcCurrency;

    private String mDstCurrency;

    private String mAmount;

    private boolean mAmountIsTarget;

    private ExchangeRate mExchangeRate;

    public static ExchangeDialogFragment newInstance(String srcCurrency, String dstCurrency,
                                                      String amount,
                                                      boolean amountIsTarget,
                                                      ExchangeRate exchangeRate) {
        Bundle args = new Bundle(5);
        ExchangeDialogFragment fragment = new ExchangeDialogFragment();
        args.putString(ARG_SRC_CURRENCY, srcCurrency);
        args.putString(ARG_DST_CURRENCY, dstCurrency);
        args.putString(ARG_AMOUNT, amount);
        args.putBoolean(ARG_AMOUNT_IS_TARGET, amountIsTarget);
        args.putSerializable(ARG_EXCHANGE_RATE, exchangeRate);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Base_Theme_AppCompat_Light_DialogWhenLarge);
        return fragment;
    }

    public interface NoticeDialogListener {
        void onExchangeComplete(DialogFragment dialog, ExchangeRateStatus status);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeDialogListener) getParentFragment();
            if (mListener == null) throw new NullPointerException();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) throw new IllegalArgumentException("no arguments");
        mSrcCurrency = arguments.getString(ARG_SRC_CURRENCY);
        mDstCurrency = arguments.getString(ARG_DST_CURRENCY);
        mAmount = arguments.getString(ARG_AMOUNT);
        mAmountIsTarget = arguments.getBoolean(ARG_AMOUNT_IS_TARGET);
        mExchangeRate = (ExchangeRate)arguments.getSerializable(ARG_EXCHANGE_RATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_exchange, container, false);
        mProgressView = root.findViewById(R.id.progress);
        mConfirmButton = root.findViewById(R.id.confirm);
        ((Toolbar)root.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClicked(v);
            }
        });
        ((TextView)root.findViewById(R.id.description)).setText(getDescription());

        setupLoadingState();
        
        return root;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mExchangeSubscription.unsubscribe();
        mProgressView = null;
    }

    private void onConfirmClicked(final View view) {
        mProgressView.setVisibility(View.VISIBLE);
        mExchangeSubscription.unsubscribe();
        final ExchangeRequest request;
        Observable<ExchangeRateStatus> observable;
        BigDecimal amount = TextUtilsW1.parseAmount(mAmount);
        if (mAmountIsTarget) {
            request = ExchangeRequest.newInstance(mSrcCurrency, mDstCurrency, amount);
        } else {
            request = ExchangeRequest.newInstanceSrcAmount(mSrcCurrency, mDstCurrency, amount);
        }

        observable =  RestClient.getApiExchanges().exchange(request);
        mExchangeSubscription = AppObservable.bindFragment(this, observable)
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupLoadingState();
                    }
                }).subscribe(new Observer<ExchangeRateStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (DBG) Log.v(TAG, "Reload data error", throwable);
                        showError(R.string.network_error, throwable);
                    }

                    @Override
                    public void onNext(ExchangeRateStatus exchangeRateStatus) {
                        if (mProgressView == null) return;
                        mProgressView.setVisibility(View.INVISIBLE);
                        view.setEnabled(true);
                        mListener.onExchangeComplete(ExchangeDialogFragment.this, exchangeRateStatus);
                        getDialog().dismiss();
                    }
                });
        setupLoadingState();
    }

    private void setupLoadingState() {
        boolean isLoading = !mExchangeSubscription.isUnsubscribed();
        mProgressView.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        mConfirmButton.setEnabled(!isLoading);
    }

    private CharSequence getDescription() {
        BigDecimal amountFrom, amountTo, amountUserInput;
        amountUserInput = TextUtilsW1.parseAmount(mAmount);
        assert amountUserInput != null;
        if (mAmountIsTarget) {
            amountTo = amountUserInput;
            amountFrom = mExchangeRate.calculateExchangeFromTarget(amountTo);
        } else {
            amountFrom = amountUserInput;
            amountTo = mExchangeRate.calculateExchangeFromSource(amountFrom);
        }
        CharSequence template = getResources().getText(R.string.exchange_xx_to_yy);
        return TextUtils.replace(template, new String[] {"$amountFrom", "$amountTo"},
                new CharSequence[] {
                        TextUtilsW1.formatAmount(amountFrom, mSrcCurrency),
                        TextUtilsW1.formatAmount(amountTo, mDstCurrency),
                });
    }

    private void showError(CharSequence defaultErrorDescription, @Nullable Throwable throwable) {
        CharSequence errText;
        if (throwable instanceof ResponseErrorException) {
            errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
        } else {
            errText = defaultErrorDescription;
        }

        if (getView() == null) return;
        Snackbar.make(getView(), errText, Snackbar.LENGTH_LONG).show();
    }

    private void showError(int defaultErrorDescription, @Nullable Throwable throwable) {
        showError(getText(defaultErrorDescription), throwable);
    }

}
