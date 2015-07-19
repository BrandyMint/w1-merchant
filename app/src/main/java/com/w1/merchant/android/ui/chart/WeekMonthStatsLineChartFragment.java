package com.w1.merchant.android.ui.chart;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.InvoiceStats;
import com.w1.merchant.android.ui.IProgressbarProvider;
import com.w1.merchant.android.ui.widget.LineChart;
import com.w1.merchant.android.utils.CurrencyHelper;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class WeekMonthStatsLineChartFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final int STATS_TYPE_WEEK = 1;

    private static final int STATS_TYPE_MONTH = 2;

    private static final String ARG_STATS_TYPE = "com.w1.merchant.android.activity.graphs.ARG_STATS_TYPE";

    private OnFragmentInteractionListener mListener;

    private TextView mAmountView;
    private TextView mPercentView;
    private LineChart mChartView;

    private List<InvoiceStats.Item> mStats;

    private int mStatsType;

    private Subscription mLoadDataSubscription = Subscriptions.unsubscribed();

    public static WeekMonthStatsLineChartFragment newInstanceWeekStats() {
        WeekMonthStatsLineChartFragment instance = new WeekMonthStatsLineChartFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_STATS_TYPE, STATS_TYPE_WEEK);
        instance.setArguments(args);
        return instance;
    }

    public static WeekMonthStatsLineChartFragment newInstanceMonthStats() {
        WeekMonthStatsLineChartFragment instance = new WeekMonthStatsLineChartFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_STATS_TYPE, STATS_TYPE_MONTH);
        instance.setArguments(args);
        return instance;
    }

    public WeekMonthStatsLineChartFragment() {
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
        mStatsType = getArguments().getInt(ARG_STATS_TYPE, STATS_TYPE_WEEK);
        mStats = new ArrayList<>(mStatsType == STATS_TYPE_WEEK ? 14 : 62);
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
        if (mListener.isCurrencyLoaded() && mStats.isEmpty()) {
            reloadData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!mStats.isEmpty()) {
                refreshDashboard(false);
            } else {
                // reloadData();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoadDataSubscription.unsubscribe();
        mAmountView = null;
        mPercentView = null;
        mChartView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onFragmentDetached(this);
        mListener = null;
    }

    public void reloadData() {
        if (DBG) Log.v(TAG, "WeekMonthStatsLineChartFragment " + mStatsType + " reloadData");
        mStats.clear();
        loadStats();
    }

    private void loadStats() {
        final Date toDate = new Date(lastUtcDays(System.currentTimeMillis(), 0));
        final Date fromDate;

        switch (mStatsType) {
            case STATS_TYPE_WEEK:
                fromDate = new Date(lastUtcDays(toDate.getTime(), 15));
                break;
            case STATS_TYPE_MONTH:
                fromDate = new Date(lastUtcDays(toDate.getTime(), 61));
                break;
            default:
                throw new IllegalStateException();
        }

        mLoadDataSubscription.unsubscribe();

        Observable<InvoiceStats> observable = AppObservable.bindFragment(this,
                RestClient.getApiInvoices().getStats(mListener.getCurrency(),
                        ISO8601Utils.format(fromDate, false, TimeZone.getTimeZone("UTC")),
                        ISO8601Utils.format(toDate, false, TimeZone.getTimeZone("UTC"))));


        NetworkUtils.StopProgressAction action = new NetworkUtils.StopProgressAction(mListener);
        mLoadDataSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(action)
                .subscribe(new Observer<InvoiceStats>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable error) {
                        if (mListener != null) {
                            CharSequence errText = ((ResponseErrorException)error).getErrorDescription(getText(R.string.network_error));
                            Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNext(InvoiceStats invoiceStats) {
                        mStats.clear();
                        mStats.addAll(invoiceStats.items);
                        refreshDashboard(true);
                    }
                });
        action.token = mListener.startProgress();
    }

    private void refreshDashboard(boolean animate) {
        if (mAmountView == null) return;
        long currentDate = System.currentTimeMillis();
        refreshGraphDataset(currentDate, animate);

        BigDecimal sumCurrentPeriod = getSumCurrentPeriod(currentDate);
        BigDecimal sumPriorPeriod = getSumPriorPeriod(currentDate);

        String amount = CurrencyHelper.formatAmount(sumCurrentPeriod.setScale(0, RoundingMode.UP),
                mListener.getCurrency());
        mAmountView.setText(amount);

        String percent;
        if (sumPriorPeriod.compareTo(BigDecimal.ZERO) > 0) {
            // ((v2-v1)/v1)*100
            percent = sumCurrentPeriod
                    .subtract(sumPriorPeriod)
                    .divide(sumPriorPeriod, BigDecimal.ROUND_HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.UP)
                    .toString() + "\u00a0%";
        } else {
            percent = "";
        }
        DayStatsLineChartFragment.setupPercent(mPercentView, percent);
    }

    private void refreshGraphDataset(long currentDate, boolean animate) {
        switch (mStatsType) {
            case STATS_TYPE_WEEK:
                refreshWeekGraphDataset(currentDate, animate);
                break;
            case STATS_TYPE_MONTH:
                refreshMonthGraphDataset(currentDate, animate);
                return ;
            default:
                throw new IllegalStateException();
        }
    }

    private void refreshWeekGraphDataset(long currentDate, boolean animate) {
        BigDecimal y[] = new BigDecimal[7];
        String x[] = new String[7];

        long startOfWeek = getCurrentPeriodStart(currentDate);

        Arrays.fill(y, BigDecimal.ZERO);
        // XXX а нахера оно не SimpleDateFormat???
        String dayOfWeek[] = getResources().getStringArray(R.array.day_of_week);
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startOfWeek);
        int startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        for (int i = 0; i < x.length; ++i) x[i] = dayOfWeek[(startDay + i) % 7];

        for (InvoiceStats.Item item: mStats) {
            if (startOfWeek > item.date.getTime()) continue;
            int diffDay = (int)((item.date.getTime() - startOfWeek) / 86400000);
            if (diffDay < y.length) {
                y[diffDay] = y[diffDay].add(item.totalAmount).subtract(item.totalCommissionAmount);
            }
        }

        setPlot(x, y, animate);
    }

    private void refreshMonthGraphDataset(long currentDate, boolean animate) {
        BigDecimal y[] = new BigDecimal[30];
        String x[] = new String[30];
        long startDate;
        Calendar calendar;
        String monthNames[];
        String dayOfWeekNames[];

        startDate = getCurrentPeriodStart(currentDate);
        Arrays.fill(y, BigDecimal.ZERO);

        // XXX а нахера оно не SimpleDateFormat???
        monthNames = getResources().getStringArray(R.array.month_array_cut);
        dayOfWeekNames = getResources().getStringArray(R.array.day_of_week);

        calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startDate);

        for (int i = 0; i < x.length; i++) {
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int monthNo = calendar.get(Calendar.MONTH);
            int dayOfWeekNo = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            x[i] = dayOfMonth + " " + monthNames[monthNo] + ", " + dayOfWeekNames[dayOfWeekNo];
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (InvoiceStats.Item item: mStats) {
            if (startDate > item.date.getTime()) continue;
            int diffDay = (int)((item.date.getTime() - startDate) / 86400000);
            if (diffDay < y.length) {
                y[diffDay] = y[diffDay].add(item.totalAmount).subtract(item.totalCommissionAmount);
            }
        }

        setPlot(x, y, animate);
    }

    private BigDecimal getSumCurrentPeriod(long currentDate) {
        BigDecimal sum = BigDecimal.ZERO;
        long start = getCurrentPeriodStart(currentDate);

        for (InvoiceStats.Item item: mStats) {
            if (start <= item.date.getTime()) {
                sum = sum.add(item.totalAmount).subtract(item.totalCommissionAmount);
            }
        }
        return sum;
    }

    public BigDecimal getSumPriorPeriod(long currentDate) {
        BigDecimal sum = BigDecimal.ZERO;
        long startCurrent = getCurrentPeriodStart(currentDate);
        long startPrior = getPriorPeriodStart(currentDate);

        for (InvoiceStats.Item item: mStats) {
            if ((startPrior <= item.date.getTime()) && (startCurrent > item.date.getTime())) {
                sum = sum.add(item.totalAmount).subtract(item.totalCommissionAmount);
            }
        }
        return sum;
    }

    private long getCurrentPeriodStart(long currentDate) {
        switch (mStatsType) {
            case STATS_TYPE_WEEK:
                return lastUtcDays(currentDate, 7);
            case STATS_TYPE_MONTH:
                return lastUtcDays(currentDate, 30);
            default:
                throw new IllegalStateException();
        }
    }

    private long getPriorPeriodStart(long currentDate) {
        switch (mStatsType) {
            case STATS_TYPE_WEEK:
                return lastUtcDays(currentDate, 14);
            case STATS_TYPE_MONTH:
                return lastUtcDays(currentDate, 60);
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Последние несколько дней, НЕ включая день currentDate.
     * Округление до суток в UTC.
     * @param currentDate текущая дата
     * @param days кол-во вычитаемых дней
     * @return дата
     */
    private long lastUtcDays(long currentDate, int days) {
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(currentDate);
        //cal.add(Calendar.DAY_OF_YEAR, 1 - days);
        cal.add(Calendar.DAY_OF_YEAR, 0 - days);

        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTimeInMillis();
    }

    private void setPlot(String dataPlotX[], BigDecimal dataPlotY[], boolean animate) {
        // add data
        ArrayList<Entry> yVals = new ArrayList<>(dataPlotY.length);
        for (int i = 0; i < dataPlotY.length; i++) {
            float value = dataPlotY[i].setScale(0, RoundingMode.UP).floatValue();
            yVals.add(new Entry(value, i, dataPlotX[i]));
        }

        // create a dataset and give it a type
        LineDataSet set = DayStatsLineChartFragment.createLineDataSet(yVals);
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

        public boolean isCurrencyLoaded();
        public String getCurrency();
    }

}
