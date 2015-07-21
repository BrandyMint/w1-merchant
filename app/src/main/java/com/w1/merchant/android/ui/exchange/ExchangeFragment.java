package com.w1.merchant.android.ui.exchange;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.CurrencyLimit;
import com.w1.merchant.android.rest.model.ExchangeRate;
import com.w1.merchant.android.rest.model.ExchangeRateStatus;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action0;
import rx.functions.Func3;
import rx.subscriptions.Subscriptions;

/**
 * Обмен валют
 */
public class ExchangeFragment extends Fragment implements  ExchangeDialogFragment.NoticeDialogListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "ExchangeFragment";

    private static final String KEY_BALANCE_LIST = "BALANCE_LIST";

    private static final String KEY_EXCHANGE_RATES = "EXCHANGE_RATES";

    private static final String KEY_USER_LIMITS = "USER_LIMITS";

    private static final String KEY_USER_INPUT_AMOUNT = "USER_INPUT_AMOUNT";

    private static final String KEY_IS_USER_INPUT_CURRENCY_TO = "IS_USER_INPUT_CURRENCY_TO";

    private static final String TAG_EXCHANGE_DIALOG = "TAG_EXCHANGE_DIALOG";

    private static final ArgbEvaluator sColorEvaluator = new ArgbEvaluator();

    private List<Balance> mBalances;

    private ExchangeRates mExchangeRates;

    private List<CurrencyLimit> mUserLimits;

    private ViewPager mFromViewPager, mToViewPager;

    private CirclePageIndicator mFromIndicator, mToIndicator;

    private TextView mExchangeRateView;

    private View mExchangeButton;

    private View mProgressView;

    private CurrencyFromViewPagerAdapter mFromAdapter;

    private CurrencyToViewPagerAdapter mToAdapter;

    private Subscription mLoadDataSubscription = Subscriptions.unsubscribed();

    private Handler mHandler;

    private CharSequence mUserInputAmount;

    private boolean mIsUserInputCurrencyTo;

    public ExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mBalances = (List<Balance>)savedInstanceState.getSerializable(KEY_BALANCE_LIST);
            if (mBalances == null) mBalances = Collections.emptyList();
            mExchangeRates = savedInstanceState.getParcelable(KEY_EXCHANGE_RATES);
            if (mExchangeRates == null) mExchangeRates = ExchangeRates.EMPTY;
            mUserLimits = (List<CurrencyLimit>)savedInstanceState.getSerializable(KEY_USER_LIMITS);
            if (mUserLimits == null) mUserLimits = Collections.emptyList();
            mUserInputAmount = savedInstanceState.getString(KEY_USER_INPUT_AMOUNT);
            mIsUserInputCurrencyTo = savedInstanceState.getBoolean(KEY_IS_USER_INPUT_CURRENCY_TO);
        } else {
            mExchangeRates = ExchangeRates.EMPTY;
            mUserLimits = Collections.emptyList();
            mBalances = Collections.emptyList();
            mUserInputAmount = null;
            mIsUserInputCurrencyTo = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exchange, container, false);
        mFromViewPager = (ViewPager)root.findViewById(R.id.currency_from_pager);
        mToViewPager = (ViewPager)root.findViewById(R.id.currency_to_pager);
        mExchangeButton = root.findViewById(R.id.exchange_button);
        mExchangeRateView = (TextView)root.findViewById(R.id.exchange_rate);
        mProgressView = root.findViewById(R.id.progress);
        mFromIndicator = (CirclePageIndicator)root.findViewById(R.id.currency_from_indicator);
        mToIndicator = (CirclePageIndicator)root.findViewById(R.id.currency_to_indicator);

        mExchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExchangeClicked(v);
            }
        });

        mFromAdapter = new CurrencyFromViewPagerAdapter(mCurrencyFromAdapterCallbacks);
        mToAdapter = new CurrencyToViewPagerAdapter(mCurrencyToViewPagerCallbacks);
        mHandler = new Handler();

        initFromViewPager();
        initToViewPager();

        refreshCurrencyFromList();
        refreshCurrencyToList();
        refreshCurrencyRateDescription();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mBalances.isEmpty()) {
            outState.putSerializable(KEY_BALANCE_LIST, new ArrayList<>(mBalances));
        }
        if (!mExchangeRates.isEmpty()) outState.putParcelable(KEY_EXCHANGE_RATES, mExchangeRates);
        if (!mUserLimits.isEmpty()) outState.putSerializable(KEY_USER_LIMITS, new ArrayList<>(mUserLimits));
        if (mUserInputAmount != null) {
            outState.putString(KEY_USER_INPUT_AMOUNT, mUserInputAmount.toString());
            outState.putBoolean(KEY_IS_USER_INPUT_CURRENCY_TO, mIsUserInputCurrencyTo);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoadDataSubscription.unsubscribe();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mFromViewPager = null;
        mToViewPager = null;
        mExchangeButton = null;
        mProgressView = null;
        mFromIndicator = null;
        mToIndicator = null;
        mToAdapter = null;
        mFromAdapter = null;
        mExchangeRateView = null;
    }

    private void onExchangeClicked(View view) {
        if (!validateForm()) return;

        if (getChildFragmentManager().findFragmentByTag(TAG_EXCHANGE_DIALOG) != null) return;
        String currencyFrom = getSelectedCurrencyFrom();
        String currencyTo = getSelectedCurrencyTo();
        ExchangeDialogFragment dialog = ExchangeDialogFragment.newInstance(
                currencyFrom,
                currencyTo,
                mUserInputAmount.toString(), mIsUserInputCurrencyTo, mExchangeRates.getRate(currencyFrom, currencyTo)
        );
        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction().addToBackStack(null);
        dialog.show(transaction, TAG_EXCHANGE_DIALOG);
    }

    private boolean validateForm() {
        String currencyFrom, currencyTo;
        BigDecimal amountUserInput, amountFrom;
        currencyFrom = getSelectedCurrencyFrom();
        currencyTo = getSelectedCurrencyTo();
        ExchangeRate exchangeRate;
        Balance balance;

        if (currencyFrom == null) {
            showError(R.string.error_no_currency_selected, null);
            return false;
        }
        if (TextUtils.isEmpty(mUserInputAmount)) {
            showError(R.string.error_amount_for_exchange_not_specified, null);
            return false;
        }

        amountUserInput = CurrencyHelper.parseAmount(mUserInputAmount);
        if (amountUserInput == null) {
            showError(R.string.error_amount_for_exchange_not_specified, null); // TODO: incorrect error text
            return false;
        }

        exchangeRate = mExchangeRates.getRate(currencyFrom, currencyTo);
        if (exchangeRate == null) {
            showError(R.string.no_currency_for_exchange, null); // TODO: incorrect error text
            return false;
        }

        if (mIsUserInputCurrencyTo) {
            amountFrom = exchangeRate.calculateExchangeFromTarget(amountUserInput);
        } else {
            amountFrom = amountUserInput;
        }

        if (amountFrom.compareTo(BigDecimal.ZERO) <= 0) {
            showError(R.string.error_amount_for_exchange_not_specified, null);
            return false;
        }

        balance = ExchangesHelper.findBalance(mBalances, currencyFrom);
        if (balance == null) {
            showError(R.string.no_currency_for_exchange, null);
            return false;
        }

        if (amountFrom.compareTo(balance.amount) > 0) {
            showError(R.string.error_not_enough_money, null);
            return false;
        }

        // TODO: 13.07.15 currency limits


        return true;
    }

    private void initFromViewPager() {
        mFromViewPager.setAdapter(mFromAdapter);
        mFromViewPager.setOffscreenPageLimit(15);

        mFromIndicator.setSnap(true);
        mFromIndicator.setViewPager(mFromViewPager);
        mFromIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            final Resources resources = getResources();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                //Log.d(TAG, "onPageScrolled() called with " + "position = [" + position + "], positionOffset = [" + positionOffset + "], positionOffsetPixels = [" + positionOffsetPixels + "]");
                if (mFromAdapter.getCount() == 0) return;
                String currency0 = mFromAdapter.getCurrency(position);
                int color0 = ExchangesHelper.getCurrencyBackgroundColor(resources, currency0);
                if (positionOffset == 0) {
                    if (DBG) Log.v(TAG, "setBackgroundColor " + color0);
                    mFromViewPager.setBackgroundColor(color0);
                } else {
                    String currency1 = mFromAdapter.getCurrency(position + 1);
                    int color1 = ExchangesHelper.getCurrencyBackgroundColor(resources, currency1);
                    int colorNew = (int) sColorEvaluator.evaluate(positionOffset, color0, color1);
                    mFromViewPager.setBackgroundColor(colorNew);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (DBG) Log.d(TAG, "currency from view pager onPageSelected() called with " + "position = [" + position + "]");
                mFromAdapter.onPageSelected(mFromViewPager, position);
                refreshCurrencyToList();
                refreshAmount();
                refreshCurrencyRateDescription();
            }
        });
    }

    private void initToViewPager() {
        mToViewPager.setAdapter(mToAdapter);
        mToViewPager.setOffscreenPageLimit(15);

        mToIndicator.setSnap(true);
        mToIndicator.setViewPager(mToViewPager);
        mToIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            final Resources resources = getResources();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                //Log.d(TAG, "onPageScrolled() called with " + "position = [" + position + "], positionOffset = [" + positionOffset + "], positionOffsetPixels = [" + positionOffsetPixels + "]");
                if (mToAdapter.getCount() == 0) return;
                String currency0 = mToAdapter.getCurrency(position);
                int color0 = ExchangesHelper.getCurrencyBackgroundColor(resources, currency0);
                if (positionOffset == 0) {
                    if (DBG) Log.v(TAG, "setBackgroundColor " + color0);
                    mToViewPager.setBackgroundColor(color0);
                } else {
                    String currency1 = mToAdapter.getCurrency(position + 1);
                    int color1 = ExchangesHelper.getCurrencyBackgroundColor(resources, currency1);
                    int colorNew = (int) sColorEvaluator.evaluate(positionOffset, color0, color1);
                    mToViewPager.setBackgroundColor(colorNew);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (DBG) Log.d(TAG, "currency to view pager onPageSelected() called with " + "position = [" + position + "]");
                mToAdapter.onPageSelected(mToViewPager, position);
                refreshAmount();
                refreshCurrencyRateDescription();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Nullable
    private String getSelectedCurrencyFrom() {
        if (mFromViewPager == null || mFromAdapter == null || mFromAdapter.getCount() == 0) return null;
        int position = mFromViewPager.getCurrentItem();
        return mFromAdapter.getCurrency(position);
    }

    @Nullable
    private String getSelectedCurrencyTo() {
        if (mToViewPager == null || mToAdapter == null || mToAdapter.getCount() == 0) return null;
        int position = mToViewPager.getCurrentItem();
        return mToAdapter.getCurrency(position);
    }

    private void setupLoadingState() {
        boolean isLoading = !mLoadDataSubscription.isUnsubscribed();
        mProgressView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mExchangeButton.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
    }

    private void setupAdapterData() {
        refreshCurrencyFromList();
        refreshCurrencyToList();
        refreshCurrencyRateDescription();
    }

    private void refreshAmount() {
        if (mIsUserInputCurrencyTo) {
            mFromAdapter.refreshAmount(mFromViewPager, mFromViewPager.getCurrentItem());
        } else {
            mToAdapter.refreshAmount(mToViewPager, mToViewPager.getCurrentItem());
        }
    }

    private void refreshCurrencyFromList() {
        if (DBG) Log.d(TAG, "refreshCurrencyFromList()");
        List<String> newList = new ArrayList<>(mFromAdapter.getCount());

        for (Balance balance: mBalances) {
            if (ExchangesHelper.isCanBeExchanged(balance, mExchangeRates)) newList.add(balance.currencyId);
        }
        if (newList.equals(mFromAdapter.getCurrencyList())) return;

        int newPosition = Math.max(0, newList.indexOf(getSelectedCurrencyFrom()));
        mFromAdapter.setCurrencyList(newList);
        mToViewPager.setCurrentItem(newPosition, false);
    }

    private void refreshCurrencyToList() {
        if (DBG) Log.d(TAG, "refreshCurrencyToList()");
        List<String> newList;
        String selectedCurrencyFrom = getSelectedCurrencyFrom();

        if (selectedCurrencyFrom == null) {
            newList = Collections.emptyList();
        } else {
            Set<String> rateSet = mExchangeRates.getDstCurrencies(selectedCurrencyFrom);
            Set<String> balancesSet = new LinkedHashSet<>(mBalances.size());
            for (Balance balance: mBalances) balancesSet.add(balance.currencyId);

            balancesSet.retainAll(rateSet);
            rateSet.removeAll(balancesSet);

            newList = new ArrayList<>(balancesSet.size() + rateSet.size());
            newList.addAll(balancesSet);
            newList.addAll(rateSet);
        }

        if (newList.equals(mToAdapter.getCurrencyList())) return;

        String selected = getSelectedCurrencyTo();
        mToAdapter.setCurrencyList(newList);
        int newPosition = Math.max(0, newList.indexOf(selected));
        mToViewPager.setCurrentItem(newPosition, false);
    }

    private void refreshCurrencyRateDescription() {
        String currencyFrom = getSelectedCurrencyFrom();
        String currencyTo = getSelectedCurrencyTo();
        if (currencyFrom == null || currencyTo == null) {
            mExchangeRateView.setText("");
            return;
        }

        ExchangeRate rate = mExchangeRates.getRate(currencyFrom, currencyTo);
        mExchangeRateView.setText(ExchangesHelper.getExchangeRateDescription(rate, getResources()));
    }

    private void reloadData() {
        if (!mLoadDataSubscription.isUnsubscribed()) mLoadDataSubscription.unsubscribe();

        Observable<List<Balance>> balanceObservable = RestClient.getApiBalance().getBalance();
        Observable<ExchangeRate.ResponseList> ratesObservable = RestClient.getApiExchanges().getRates();
        Observable<List<CurrencyLimit>> limitsObservable = RestClient.getApiLimits().getLimits();

        Observable<LoadDataHolder> s =  Observable.zip(balanceObservable, ratesObservable,
                limitsObservable, sLoadDataZipFunc);
        mLoadDataSubscription = AppObservable.bindFragment(this, s)
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupLoadingState();
                    }
                })
                .subscribe(new Observer<LoadDataHolder>() {
                    @Override
                    public void onCompleted() {
                        setupAdapterData();
                        if (!ExchangesHelper.containsCurrencyCanBeExchanged(mBalances, mExchangeRates)) {
                            showError(R.string.no_currency_for_exchange, null);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (DBG) Log.v(TAG, "Reload data error", throwable);
                        showError(R.string.network_error, throwable);
                    }

                    @Override
                    public void onNext(LoadDataHolder data) {
                        mBalances = data.balances;
                        mExchangeRates = data.exchanges;
                        mUserLimits = data.limits;
                    }
                });
        setupLoadingState();
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

    @Override
    public void onExchangeComplete(DialogFragment dialog, ExchangeRateStatus status) {
        mIsUserInputCurrencyTo = false;
        mUserInputAmount = null;
        if (mFromViewPager != null) mFromViewPager.setCurrentItem(0, false);
        if (mToViewPager != null) mToViewPager.setCurrentItem(0, false);
        mFromAdapter.refreshAmount(mFromViewPager, 0);
        mToAdapter.refreshAmount(mToViewPager, 0);
        Snackbar.make(getView(), ExchangesHelper.getExchangeRateStatusDescription(status, getResources()), Snackbar.LENGTH_LONG).show();
        reloadData();
    }

    private static class LoadDataHolder {
        final List<Balance> balances;
        final ExchangeRates exchanges;
        final List<CurrencyLimit> limits;

        public LoadDataHolder(List<Balance> balances, ExchangeRates exchanges,
                              List<CurrencyLimit> limits) {
            this.balances = balances;
            this.exchanges = exchanges;
            this.limits = limits;
        }
    }

    private final CurrencyFromViewPagerAdapter.Callbacks mCurrencyFromAdapterCallbacks = new CurrencyFromViewPagerAdapter.Callbacks() {

        @Override
        public Collection<Balance> getBalanceList() {
            return mBalances;
        }

        @Override
        public CharSequence getUserAmountFrom(String currencyFrom) {
            if (mIsUserInputCurrencyTo) {
                BigDecimal currencyToValue = CurrencyHelper.parseAmount(mUserInputAmount);
                String currencyTo = getSelectedCurrencyTo();
                ExchangeRate rate = mExchangeRates.getRate(currencyFrom, currencyTo);
                if (rate == null || currencyToValue == null) return null;
                BigDecimal amount = rate.calculateExchangeFromTarget(currencyToValue).setScale(2, RoundingMode.UP);
                if (BigDecimal.ZERO.compareTo(amount) == 0) {
                    return null;
                } else {
                    return amount.toPlainString();
                }
            } else {
                return mUserInputAmount;
            }
        }

        @Override
        public void onAmountFromTextChanged(Editable s) {
            mIsUserInputCurrencyTo = false;
            mUserInputAmount = s;
            mHandler.removeCallbacks(mRefreshAmountRunnable);
            mHandler.post(mRefreshAmountRunnable);
        }
    };

    private final CurrencyToViewPagerAdapter.Callbacks mCurrencyToViewPagerCallbacks = new CurrencyToViewPagerAdapter.Callbacks() {

        @Override
        public Collection<Balance> getBalanceList() {
            return mBalances;
        }

        @Override
        public CharSequence getUserAmountTo(String currencyTo) {
            if (!mIsUserInputCurrencyTo) {
                BigDecimal currencyFromValue = CurrencyHelper.parseAmount(mUserInputAmount);
                String currencyFrom = getSelectedCurrencyFrom();
                ExchangeRate rate = mExchangeRates.getRate(currencyFrom, currencyTo);
                if (rate == null || currencyFromValue == null) return null;
                BigDecimal amount =  rate.calculateExchangeFromSource(currencyFromValue).setScale(2, RoundingMode.DOWN);
                if (BigDecimal.ZERO.compareTo(amount) == 0) {
                    return null;
                } else {
                    return amount.toPlainString();
                }
            } else {
                return mUserInputAmount;
            }
        }

        @Override
        public void onAmountToTextChanged(Editable s) {
            mIsUserInputCurrencyTo = true;
            mUserInputAmount = s;
            mHandler.removeCallbacks(mRefreshAmountRunnable);
            mHandler.post(mRefreshAmountRunnable);
        }
    };

    private Runnable mRefreshAmountRunnable = new Runnable() {
        @Override
        public void run() {
            refreshAmount();
        }
    };

    private static final Func3<List<Balance>, ExchangeRate.ResponseList, List<CurrencyLimit>, LoadDataHolder>
            sLoadDataZipFunc = new Func3<List<Balance>, ExchangeRate.ResponseList, List<CurrencyLimit>, LoadDataHolder>() {

        @Override
        public LoadDataHolder call(List<Balance> balances,
                                   ExchangeRate.ResponseList rates, List<CurrencyLimit> currencyLimits) {
            return new LoadDataHolder(balances, new ExchangeRates(rates.items), currencyLimits);
        }
    };
}
