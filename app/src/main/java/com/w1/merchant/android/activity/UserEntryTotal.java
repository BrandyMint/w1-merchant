package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import retrofit.Callback;
import retrofit.client.Response;

public class UserEntryTotal extends Activity {

    private static final String ARG_CURRENCY = "com.w1.merchant.android.activity.ARG_CURRENCY";

    private static final String ARG_DATE_FROM = "com.w1.merchant.android.activity.ARG_DATE_FROM";

    private static final String ARG_DATE_TO = "com.w1.merchant.android.activity.ARG_DATE_TO";

    private String mCurrency;

    private Date mDateFrom;

    private Date mDateTo;

    private int mProgressCount;

    private ApiUserEntry mApiUserEntry;

    private boolean mStopAll = false;

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

        mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);

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
        mStopAll = false;
        startLoadSummary();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStopAll = true;
    }

    private void startLoadSummary() {
        mProgressCount = 2;
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        startLoadIncomingSummary();
        startLoadOutcomingSummary();
    }

    private void startLoadIncomingSummary() {

        new ApiRequestTask<TransactionHistory>() {

            final String dateFrom = ISO8601Utils.format(mDateFrom);
            final String dateTo = ISO8601Utils.format(mDateTo);

            int pageNo = 1;

            BigDecimal summ = new BigDecimal(0);
            BigDecimal commission = new BigDecimal(0);

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {
                mApiUserEntry.getEntries(pageNo, 1000, dateFrom, dateTo, null, null,
                        mCurrency, null, TransactionHistoryEntry.DIRECTION_INCOMING, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return UserEntryTotal.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mStopAll) return;
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(UserEntryTotal.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
            }

            @Override
            protected void onSuccess(TransactionHistory transactionHistory, Response response) {
                if (mStopAll) return;
                for (TransactionHistoryEntry entry: transactionHistory.items) {
                    summ = summ.add(entry.amount);
                    commission = commission.add(entry.commissionAmount);
                }
                if (transactionHistory.items.isEmpty()) {
                    onIncomingSummaryCompleted(summ, commission);
                } else {
                    pageNo += 1;
                    execute();
                }

            }
        }.execute();
    }

    private void startLoadOutcomingSummary() {
        new ApiRequestTask<TransactionHistory>() {

            final String dateFrom = ISO8601Utils.format(mDateFrom);
            final String dateTo = ISO8601Utils.format(mDateTo);

            int pageNo = 1;

            BigDecimal summ = new BigDecimal(0);
            BigDecimal commission = new BigDecimal(0);

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {
                mApiUserEntry.getEntries(pageNo, 1000, dateFrom, dateTo, null, null,
                        mCurrency, null, TransactionHistoryEntry.DIRECTION_OUTCOMING, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return UserEntryTotal.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mStopAll) return;
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(UserEntryTotal.this, errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
            }

            @Override
            protected void onSuccess(TransactionHistory transactionHistory, Response response) {
                if (mStopAll) return;
                for (TransactionHistoryEntry entry: transactionHistory.items) {
                    summ = summ.add(entry.amount);
                    commission = commission.add(entry.commissionAmount);
                }
                if (transactionHistory.items.isEmpty()) {
                    onOutcomingSummaryCompleted(summ, commission);
                } else {
                    pageNo += 1;
                    execute();
                }
            }
        }.execute();
    }

    void onIncomingSummaryCompleted(BigDecimal amount, BigDecimal commission) {
        ((TextView)findViewById(R.id.summ_inc)).setText(buildValue(R.string.sum_period, amount));
        ((TextView)findViewById(R.id.comis_inc)).setText(buildValue(R.string.comis_period, commission));
        mProgressCount -= 1;
        if (mProgressCount <= 0) findViewById(R.id.progress).setVisibility(View.GONE);
    }

    void onOutcomingSummaryCompleted(BigDecimal amount, BigDecimal commission) {
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
}
