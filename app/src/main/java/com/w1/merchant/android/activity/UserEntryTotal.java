package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.TransactionHistory;
import com.w1.merchant.android.model.TransactionHistoryEntry;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class UserEntryTotal extends Activity {

    private static final String ARG_CURRENCY = "com.w1.merchant.android.activity.ARG_CURRENCY";

    private static final String ARG_DATE_FROM = "com.w1.merchant.android.activity.ARG_DATE_FROM";

    private static final String ARG_DATE_TO = "com.w1.merchant.android.activity.ARG_DATE_TO";

    private String mCurrency;

    private Date mDateFrom;

    private Date mDateTo;

    private int mProgressCount;

    private SummaryLoader mIncomingSummaryLoader;

    private SummaryLoader mOutgoingSummaryLoader;

    public static void startActivity(Context context, Date from, Date to, String currency) {
        Intent intent = new Intent(context, UserEntryTotal.class);
        intent.putExtra(ARG_CURRENCY, currency);
        intent.putExtra(ARG_DATE_FROM, from.getTime());
        intent.putExtra(ARG_DATE_TO, to.getTime());
        context.startActivity(intent);
    }

    public UserEntryTotal() {
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userentry_total);

        mCurrency = getIntent().getStringExtra(ARG_CURRENCY);
        mDateFrom = new Date(getIntent().getLongExtra(ARG_DATE_FROM, System.currentTimeMillis()));
        mDateTo = new Date(getIntent().getLongExtra(ARG_DATE_TO, System.currentTimeMillis()));

		findViewById(R.id.ivBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
	}

    @Override
    protected void onStart() {
        super.onStart();
        startLoadSummary();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (mIncomingSummaryLoader != null) {
                mIncomingSummaryLoader.cancel();
                mIncomingSummaryLoader = null;
            }
            if (mOutgoingSummaryLoader != null) {
                mOutgoingSummaryLoader.cancel();
                mOutgoingSummaryLoader = null;
            }
        }
    }

    private void startLoadSummary() {
        mProgressCount = 2;
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        startLoadIncomingSummary();
        startLoadOutgoingSummary();
    }

    private void startLoadIncomingSummary() {

        if (mIncomingSummaryLoader != null) {
            mIncomingSummaryLoader.cancel();
        }

        mIncomingSummaryLoader = new SummaryLoader(this, mDateFrom, mDateTo,
                TransactionHistoryEntry.DIRECTION_INCOMING, mCurrency) {
            @Override
            public void onError(Throwable e) {
                CharSequence errText = ((NetworkUtils.ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                Toast toast = Toast.makeText(UserEntryTotal.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            public void onCompleted(BigDecimal summ, BigDecimal commission) {
                onIncomingSummaryCompleted(summ, commission);
            }
        };
        mIncomingSummaryLoader.start();
    }

    private void startLoadOutgoingSummary() {
        if (mOutgoingSummaryLoader != null) {
            mOutgoingSummaryLoader.cancel();
        }

        mOutgoingSummaryLoader = new SummaryLoader(this, mDateFrom, mDateTo,
                TransactionHistoryEntry.DIRECTION_OUTGOING, mCurrency) {
            @Override
            public void onError(Throwable e) {
                CharSequence errText = ((NetworkUtils.ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                Toast toast = Toast.makeText(UserEntryTotal.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            public void onCompleted(BigDecimal summ, BigDecimal commission) {
                onOutgoingSummaryCompleted(summ, commission);
            }
        };
        mOutgoingSummaryLoader.start();

    }

    void onIncomingSummaryCompleted(BigDecimal amount, BigDecimal commission) {
        ((TextView)findViewById(R.id.summ_inc)).setText(buildValue(R.string.sum_period, amount));
        ((TextView)findViewById(R.id.comis_inc)).setText(buildValue(R.string.comis_period, commission));
        mProgressCount -= 1;
        if (mProgressCount <= 0) findViewById(R.id.progress).setVisibility(View.GONE);
    }

    void onOutgoingSummaryCompleted(BigDecimal amount, BigDecimal commission) {
        ((TextView)findViewById(R.id.summ_out)).setText(buildValue(R.string.sum_period, amount));
        ((TextView)findViewById(R.id.comis_out)).setText(buildValue(R.string.comis_period, commission));
        mProgressCount -= 1;
        if (mProgressCount <= 0) findViewById(R.id.progress).setVisibility(View.GONE);
    }

    private Spanned buildValue(int nameRes, BigDecimal value) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(getText(nameRes));
        ssb.append('\u00a0');
        ssb.append(TextUtilsW1.formatNumber(value.setScale(0, RoundingMode.HALF_UP)));
        ssb.append('\u00a0');
        ssb.append(TextUtilsW1.getCurrencySymbol2(mCurrency, 2));
        return ssb;
    }

    public static abstract class SummaryLoader {

        private final ApiUserEntry mApiUserEntry;

        private final Date mDateFrom;

        private final Date mDateTo;

        private final String mCurrency;

        private final String mDirection;

        private int mPageNo = 1;

        private boolean mCancelled;

        private Subscription mSubscription  = Subscriptions.unsubscribed();

        private BigDecimal mSum;

        private BigDecimal mCommission;

        private final Activity mActivity;

        public SummaryLoader(Activity activity, Date dateFrom, Date dateTo, String direction, String currency) {
            mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);
            mActivity = activity;
            mDateFrom = dateFrom;
            mDateTo = dateTo;
            mCurrency = currency;
            mDirection = direction;
        }

        public void start() {
            BigDecimal mSum = new BigDecimal(0);
            BigDecimal mCommission = new BigDecimal(0);
            mPageNo = 1;
            doRequest();
        }

        private void doRequest() {
            if (mCancelled) return;

            final String dateFrom = ISO8601Utils.format(mDateFrom);
            final String dateTo = ISO8601Utils.format(mDateTo);

            Observable<TransactionHistory> observable =
                    AppObservable.bindActivity(mActivity,
                    mApiUserEntry.getEntries(mPageNo, 1000, dateFrom, dateTo, null, null,
                            mCurrency, null, mDirection));

            mSubscription = observable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .retryWhen(new RetryWhenCaptchaReady(mActivity))
                    .subscribe(new Observer<TransactionHistory>() {
                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            if (mCancelled) return;
                            SummaryLoader.this.onError(e);
                        }

                        @Override
                        public void onNext(TransactionHistory transactionHistory) {
                            if (mCancelled) return;
                            for (TransactionHistoryEntry entry: transactionHistory.items) {
                                mSum = mSum.add(entry.amount);
                                mCommission = mCommission.add(entry.commissionAmount);
                            }
                            if (transactionHistory.items.isEmpty()) {
                                SummaryLoader.this.onCompleted(mSum, mCommission);
                            } else {
                                mPageNo += 1;
                                doRequest();
                            }
                        }
                    });

        }

        public void cancel() {
            mCancelled  = true;
            mSubscription.unsubscribe();
        }

        public abstract void onError(Throwable e);

        public abstract void onCompleted(BigDecimal summ, BigDecimal commission);

    }
}
