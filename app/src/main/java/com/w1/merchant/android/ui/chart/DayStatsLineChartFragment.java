package com.w1.merchant.android.ui.chart;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.ui.IProgressbarProvider;
import com.w1.merchant.android.ui.widget.LineChart;
import com.w1.merchant.android.rest.model.TransactionHistory;
import com.w1.merchant.android.rest.model.TransactionHistoryEntry;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class DayStatsLineChartFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private OnFragmentInteractionListener mListener;

    private TextView mAmountView;
    private TextView mPercentView;
    private LineChart mChartView;

    private Collection<TransactionHistoryEntry> mHistory2day;

    private TransactionStatsDaysDataLoader mLoadTask;

    public static void setupPercent(TextView precentView, String text) {
        if (text.isEmpty()) {
            precentView.setText("");
            precentView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else if (text.startsWith("-")) {
            precentView.setText(text.replace("-", ""));
            precentView.setTextColor(Color.RED);
            precentView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.down, 0, 0, 0);
        } else {
            precentView.setText(text);
            precentView.setTextColor(Color.parseColor("#9ACD32"));
            precentView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.up, 0, 0, 0);
        }
    }

    public static LineDataSet createLineDataSet(ArrayList<Entry> enrtries) {
        LineDataSet set = new LineDataSet(enrtries, "DataSet 1");
        set.setColor(Color.parseColor("#ADFF2F"));
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(117, 117, 117));
        set.setDrawCircles(false);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.07f);
        set.setDrawValues(false);

        //set.enableDashedLine(1, 1, 1); // Заготовка для HackyPaint
        return set;
    }

    public static DayStatsLineChartFragment newInstance() {
        return new DayStatsLineChartFragment();
    }

    public DayStatsLineChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) getParentFragment();
            if (mListener == null) mListener = (OnFragmentInteractionListener)getActivity();
            mListener.onFragmentAttached(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistory2day = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_graph, container, false);
        mAmountView = (TextView)root.findViewById(R.id.amount);
        mPercentView = (TextView)root.findViewById(R.id.percent);
        mChartView = (LineChart)root.findViewById(R.id.chart);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mListener.isCurrencyLoaded() && mHistory2day.isEmpty()) {
            reloadData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!mHistory2day.isEmpty()) {
                refreshDashboard(false);
            } else {
                // reloadData();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLoadTask != null && !mLoadTask.isCancelled()) {
            mLoadTask.cancel();
            mListener.stopProgress(mLoadTask);
            mLoadTask = null;
        }
        mAmountView = null;
        mPercentView = null;
        mChartView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mLoadTask != null) mLoadTask.cancel();
        mListener.onFragmentDetached(this);
        mListener = null;
    }

    public void reloadData() {
        if (DBG) Log.v(TAG, "DayStatsLineChartFragment reloadData");
        mHistory2day.clear();
        refresh2DayData();
    }

    private void refresh2DayData() {
        final Calendar cal2aysAgo = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal2aysAgo.add(Calendar.DAY_OF_YEAR, -2);

        if (mLoadTask != null) mLoadTask.cancel();

        final NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);

        mLoadTask = new TransactionStatsDaysDataLoader(this, mListener.getCurrency()) {

            @Override
            public void onError(Throwable error) {
                stopProgressAction.call();
                if (mListener != null) {
                    if (getActivity() != null) {
                        CharSequence errText = ((ResponseErrorException) error).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }
                }
            }

            @Override
            public void onCompleted(Collection<TransactionHistoryEntry> transactions) {
                stopProgressAction.call();
                if (mListener != null) {
                    mHistory2day.clear();
                    mHistory2day.addAll(transactions);
                    refreshDashboard(true);
                }
            }

            @Override
            public void onUnsubscribe() {
                stopProgressAction.call();
            }
        };

        stopProgressAction.token = mListener.startProgress();
        mLoadTask.start();
    }

    private void refreshDashboard(boolean animate) {
        if (mAmountView == null) return;
        long currentDate = System.currentTimeMillis();
        refreshGraphDataset(currentDate, animate);

        BigDecimal sumCurrentDay = getSumCurrentDay(currentDate);
        BigDecimal sumLastDay = getSumLastDay(currentDate);

        Spanned currencySumbol =  TextUtilsW1.getCurrencySymbol2(mListener.getCurrency(), 1);
        SpannableStringBuilder sumDay = new SpannableStringBuilder(TextUtilsW1.formatNumber(
                sumCurrentDay.setScale(0, RoundingMode.UP)));
        sumDay.append('\u00a0');
        sumDay.append(currencySumbol);
        mAmountView.setText(sumDay);

        String percentDay;
        if (sumLastDay.compareTo(BigDecimal.ZERO) > 0) {
            // ((v2-v1)/v1)*100
            percentDay = sumCurrentDay
                    .subtract(sumLastDay)
                    .divide(sumLastDay, BigDecimal.ROUND_HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_EVEN)
                    .toString() + "\u00a0%";
        } else {
            percentDay = "";
        }
        setupPercent(mPercentView, percentDay);
    }

    private void refreshGraphDataset(long currentDate, boolean animate) {
        int diffHour;
        Calendar calendar;

        BigDecimal y[] = new BigDecimal[24];
        String x[] = new String[24];
        String hourSuffix = " " + getString(R.string.hour_cut);

        Arrays.fill(y, BigDecimal.ZERO);
        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
        calendar.setTimeInMillis(currentDate);
        calendar.add(Calendar.HOUR_OF_DAY, -23);

        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        for (int i = 0; i < 24; ++i) x[i] = (startHour + i) % 24 + hourSuffix;

        for (TransactionHistoryEntry entry: mHistory2day) {
            diffHour = (int)(Math.abs(currentDate - entry.getUpdateOrCreateDate().getTime()) / 3600000l);
            if (diffHour < 24) {
                y[23 - diffHour] = y[23 - diffHour].add(entry.amount).subtract(entry.commissionAmount);
            }
        }

        setPlot(x, y, animate);
    }

    private BigDecimal getSumCurrentDay(long currentDate) {
        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionHistoryEntry entry: mHistory2day) {
            int diffHour = (int)(Math.abs(currentDate - entry.getUpdateOrCreateDate().getTime()) / 3600000l);
            if (diffHour < 24) {
                sum = sum.add(entry.amount).subtract(entry.commissionAmount);
            }
        }
        return sum;
    }

    public BigDecimal getSumLastDay(long currentDate) {
        BigDecimal sum = BigDecimal.ZERO;
        for (TransactionHistoryEntry entry: mHistory2day) {
            int diffHour = (int)(Math.abs(currentDate - entry.getUpdateOrCreateDate().getTime()) / 3600000l);
            if (diffHour >= 24 && diffHour < 48) {
                sum = sum.add(entry.amount).subtract(entry.commissionAmount);
            }
        }
        return sum;
    }

    private void setPlot(String dataPlotX[], BigDecimal dataPlotY[], boolean animate) {
        // add data
        ArrayList<Entry> yVals = new ArrayList<>(dataPlotY.length);
        for (int i = 0; i < dataPlotY.length; i++) {
            float value = dataPlotY[i].setScale(0, RoundingMode.UP).floatValue();
            yVals.add(new Entry(value, i, dataPlotX[i]));
        }

        // create a dataset and give it a type
        LineDataSet set = createLineDataSet(yVals);
        LineData data = new LineData(dataPlotX, set);

        // set data
        mChartView.setData(data);
        if (animate) {
            int animTime = getResources().getInteger(R.integer.graphics_anim_time);
            mChartView.animateXY(animTime, animTime);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener extends IProgressbarProvider {
        public void onFragmentAttached(Fragment fragment);
        public void onFragmentDetached(Fragment fragment);
        public String getCurrency();
        public boolean isCurrencyLoaded();
    }

    private static abstract class TransactionStatsDaysDataLoader {

        private final String mDateFrom;

        private final String mCurrency;

        private int mPageNo = 1;

        private boolean mCancelled;

        private Subscription mSubscription  = Subscriptions.unsubscribed();

        private final List<TransactionHistoryEntry> mHistory;

        private final Fragment mFragment;

        public TransactionStatsDaysDataLoader(Fragment fragment, String currency) {
            mFragment = fragment;
            mHistory = new ArrayList<>();
            mCurrency = currency;
            final Calendar cal2DaysAgo = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            cal2DaysAgo.add(Calendar.DAY_OF_MONTH, -2);
            mDateFrom = ISO8601Utils.format(cal2DaysAgo.getTime());
        }

        public void start() {
            mHistory.clear();
            mPageNo = 1;
            doRequest();
        }

        private void doRequest() {
            if (mCancelled) return;

            Observable<TransactionHistory> observable = AppObservable.bindFragment(mFragment,
                    RestClient.getApiUserEntry().getEntries(mPageNo, 1000, mDateFrom, null, null, null,
                            mCurrency, null, TransactionHistoryEntry.DIRECTION_INCOMING));

            mSubscription = observable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .retryWhen(new RetryWhenCaptchaReady(mFragment))
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            if (mCancelled) TransactionStatsDaysDataLoader.this.onUnsubscribe();
                        }
                    })
                    .subscribe(new Observer<TransactionHistory>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (mCancelled) return;
                            TransactionStatsDaysDataLoader.this.onError(e);
                        }

                        @Override
                        public void onNext(TransactionHistory transactionHistory) {
                            if (mCancelled) return;
                            if (!transactionHistory.items.isEmpty()) {
                                mHistory.addAll(transactionHistory.items);
                                mPageNo += 1;
                                if (mPageNo < 300) {
                                    doRequest();
                                } else {
                                    TransactionStatsDaysDataLoader.this.sortResultOnCompleted();
                                }
                            } else {
                                TransactionStatsDaysDataLoader.this.sortResultOnCompleted();
                            }
                        }
                    });
        }

        void sortResultOnCompleted() {
            Map<BigInteger, TransactionHistoryEntry> uniqMap = new HashMap<>(mHistory.size());
            for (TransactionHistoryEntry entry: mHistory) uniqMap.put(entry.entryId, entry);

            mHistory.clear();
            onCompleted(uniqMap.values());
        }

        public void cancel() {
            if (!mCancelled) {
                mCancelled = true;
                mSubscription.unsubscribe();
            }
        }

        public boolean isCancelled() {
            return mCancelled;
        }

        public abstract void onError(Throwable e);

        public abstract void onCompleted(Collection<TransactionHistoryEntry> history);

        public abstract void onUnsubscribe();

    }


}
