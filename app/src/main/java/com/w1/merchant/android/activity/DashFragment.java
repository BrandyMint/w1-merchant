package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MarkerView;
import com.github.mikephil.charting.utils.Utils;
import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.MainVPAdapter;
import com.w1.merchant.android.extra.UserEntryAdapter2;
import com.w1.merchant.android.model.Balance;
import com.w1.merchant.android.model.TransactionHistory;
import com.w1.merchant.android.model.TransactionHistoryEntry;
import com.w1.merchant.android.service.ApiBalance;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.client.Response;

public class DashFragment extends Fragment {

    public static final int PLOT_24 = 1;
    public static final int PLOT_WEEK = 2;
    public static final int PLOT_30 = 3;
    public static final int TRANSACTION_HISTORY_ITEMS_PER_PAGE = 25;
    public ListView lvDash;
    TextView tvPercent;

    ViewPager vpDash;
    PagerAdapter pagerAdapter;
    SegmentedRadioGroup srgDash;
    RadioButton rbHour, rbWeek, rbMonth;
    private TextView mFooter;
    private View llHeader;
    //XYSeries series1;
    SwipeRefreshLayout swipeLayout;
    LineChart mChart;
    ImageView ivPercent;

    private int mCurrentPage = 1;
    private int mCurrentPageUEGraph = 1;

    public int currentPlot = PLOT_24;

    public ArrayList<Integer> dataPlotDay, dataPlotWeek, dataPlotMonth;
    public ArrayList<String> dataPlotDayX, dataPlotWeekX,
            dataPlotMonthX, dataDayWeekMonth, dataDWMCurrency;

    public String percentDay = "";
    public String percentWeek = "";
    public String percentMonth = "";

    private OnFragmentInteractionListener mListener;

    private UserEntryAdapter2 mAdapter;
    private ApiUserEntry mApiUserEntry;
    private ApiBalance mApiBalance;

    private Collection<TransactionHistoryEntry> mHistory60day;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);
        mApiBalance = NetworkUtils.getInstance().createRestAdapter().create(ApiBalance.class);
        dataPlotDay = new ArrayList<>();
        dataPlotWeek = new ArrayList<>();
        dataPlotMonth = new ArrayList<>();
        dataPlotDayX = new ArrayList<>();
        dataPlotWeekX = new ArrayList<>();
        dataPlotMonthX = new ArrayList<>();
        mHistory60day = new ArrayList<>();
        dataDayWeekMonth = new ArrayList<>();
        dataDWMCurrency = new ArrayList<>();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.dashboard, container, false);
        //llPlot = (LinearLayout) llHeader.findViewById(R.id.llPlot);
        lvDash = (ListView) parentView.findViewById(R.id.lvDash);

        mFooter = (TextView)inflater.inflate(R.layout.footer2, lvDash, false);
        llHeader = inflater.inflate(R.layout.dash_header, lvDash, false);

        mChart = (LineChart) llHeader.findViewById(R.id.chart1);
        tvPercent = (TextView) llHeader.findViewById(R.id.tvPercent);
        ivPercent = (ImageView) llHeader.findViewById(R.id.ivPercent);

        srgDash = (SegmentedRadioGroup) llHeader.findViewById(R.id.srgDash);
        rbHour = (RadioButton) llHeader.findViewById(R.id.rbHour);
        rbHour.setChecked(true);
        rbWeek = (RadioButton) llHeader.findViewById(R.id.rbWeek);
        rbMonth = (RadioButton) llHeader.findViewById(R.id.rbMonth);
        swipeLayout = (SwipeRefreshLayout) parentView;
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                rbHour.setChecked(true);
                refreshDashboard();
            }
        });
        mAdapter = new UserEntryAdapter2(getActivity());

        return parentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDashboard();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void refreshDashboard() {
        clearDataArrays();
        mCurrentPageUEGraph = 1;
        mCurrentPage = 1;
        refreshBalance();
    }

    private void clearDataArrays() {
        dataPlotDay.clear();
        dataPlotWeek.clear();
        dataPlotMonth.clear();
        dataPlotDayX.clear();
        dataPlotWeekX.clear();
        dataPlotMonthX.clear();
        mHistory60day.clear();
    }


    public void createListView() {
        //заполнение ListView
        swipeLayout.setRefreshing(false);
        if (lvDash.getHeaderViewsCount() == 0) {
            lvDash.addHeaderView(llHeader);
        }
        if ((mAdapter.getCount() > TRANSACTION_HISTORY_ITEMS_PER_PAGE - 1) & (lvDash.getFooterViewsCount() == 0)) {
            lvDash.addFooterView(mFooter);
        }
        if ((mAdapter.getCount() < TRANSACTION_HISTORY_ITEMS_PER_PAGE) & (mCurrentPage == 1)) {
            lvDash.removeFooterView(mFooter);
        }
        lvDash.setAdapter(mAdapter);

        lvDash.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                TransactionHistoryEntry entry = (TransactionHistoryEntry) parent.getItemAtPosition(position);
                Details.startActivity(getActivity(), entry, view);
            }
        });

        lvDash.setOnScrollListener(new AbsListView.OnScrollListener() {
            private Rect scrollBounds = new Rect();

            @Override
            public void onScroll(AbsListView arg0,
                                 int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // when list scrolling stops
                        view.getHitRect(scrollBounds);
                        if (mFooter.isShown() && mFooter.getLocalVisibleRect(scrollBounds)) {
                            Log.v("DashFragment", "loading");
                            mCurrentPage += 1;
                            mFooter.setText(R.string.loading);
                            refreshTransactionHistory();
                        }
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }
        });
    }

    void refreshTransactionHistory() {
        mListener.startProgress();
        new ApiRequestTask<TransactionHistory>() {

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {
                mApiUserEntry.getEntries(mCurrentPage, TRANSACTION_HISTORY_ITEMS_PER_PAGE,
                        null, null, null, null,
                        mListener.getCurrency(),
                        null, null, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return DashFragment.this.getActivity();
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mListener != null) {
                    mListener.stopProgress();
                    CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                    Toast toast = Toast.makeText(getContainerActivity(), errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                }
            }

            @Override
            protected void onCancelled() {
                if (mListener != null) mListener.stopProgress();
            }

            @Override
            protected void onSuccess(TransactionHistory transactionHistory, Response response) {
                if (mListener != null) {
                    mListener.stopProgress();
                    addTransactionHistoryResult(transactionHistory);
                }
            }
        }.execute();
    }

    private void addTransactionHistoryResult(TransactionHistory newData) {
        if (newData != null) {
            if (mCurrentPage == 1) {
                mAdapter.setItems(newData.items);
                createListView();
            } else {
                mAdapter.addItems(newData.items);
                mFooter.setText(R.string.data_load);
            }
            if (newData.items.size() == 0) removeFooter();
        } else {
            removeFooter();
        }
        if (getView() != null && llHeader != null) {
            llHeader.findViewById(R.id.empty_text).setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    public void removeFooter() {
        lvDash.removeFooterView(mFooter);
    }

    public void setViewPager() {
        srgDash.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbHour:
                        vpDash.setCurrentItem(0, true);
                        createPlot(dataPlotDayX, dataPlotDay);
                        currentPlot = PLOT_24;
                        setPercent(percentDay);
                        break;
                    case R.id.rbWeek:
                        vpDash.setCurrentItem(1);
                        createPlot(dataPlotWeekX, dataPlotWeek);
                        currentPlot = PLOT_WEEK;
                        setPercent(percentWeek);
                        break;
                    case R.id.rbMonth:
                        vpDash.setCurrentItem(2);
                        createPlot(dataPlotMonthX, dataPlotMonth);
                        currentPlot = PLOT_30;
                        setPercent(percentMonth);
                        break;
                    default:
                        break;
                }
            }
        });

        vpDash = (ViewPager) llHeader.findViewById(R.id.vpDash);
        pagerAdapter = new MainVPAdapter(getActivity(),
                dataDayWeekMonth, dataDWMCurrency);
        vpDash.setAdapter(pagerAdapter);
        vpDash.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Log.d("1", "onPageSelected, position = " + position);
                if (position == 0) {
                    rbHour.setChecked(true);
                } else if (position == 1) {
                    rbWeek.setChecked(true);
                } else {
                    rbMonth.setChecked(true);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
    }


    private void refreshBalance() {
        mListener.startProgress();
        new ApiRequestTask<List<Balance>>() {

            @Override
            protected void doRequest(Callback<List<Balance>> callback) {
                mApiBalance.getBalance(callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return DashFragment.this.getActivity();
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mListener != null) {
                    mListener.stopProgress();
                    CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                    Toast toast = Toast.makeText(getContainerActivity(), errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                }
            }

            @Override
            protected void onCancelled() {
                if (mListener != null) mListener.stopProgress();
            }

            @Override
            protected void onSuccess(List<Balance> balances, Response response) {
                if (mListener != null) {
                    mListener.stopProgress();
                    mListener.onBalanceLoaded(balances);
                    mCurrentPageUEGraph = 1;
                    refreshTransactionHistory();
                    refresh60DayData();
                }
            }
        }.execute();
    }

    private void refresh60DayData() {
        mListener.startProgress();
        new ApiRequestTask<TransactionHistory>() {

            final String date60DaysAgo = ISO8601Utils.format(getDate60DaysAgo());

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {
                mApiUserEntry.getEntries(mCurrentPageUEGraph, 1000, date60DaysAgo, null,
                        null, null, mListener.getCurrency(), null, TransactionHistoryEntry.DIRECTION_INCOMING, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return DashFragment.this.getActivity();
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mListener != null) {
                    mListener.stopProgress();
                    CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                    Toast toast = Toast.makeText(getContainerActivity(), errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                }
            }

            @Override
            protected void onCancelled() {
                if (mListener != null) mListener.stopProgress();
            }

            @Override
            protected void onSuccess(TransactionHistory transactionHistory, Response response) {
                if (mListener != null) {
                    if (!transactionHistory.items.isEmpty()) {
                        mHistory60day.addAll(transactionHistory.items);
                        mCurrentPageUEGraph += 1;
                        if (mCurrentPageUEGraph < 300) execute();
                    } else {
                        mListener.stopProgress();
                        Map<BigInteger, TransactionHistoryEntry> uniqMap = new HashMap<>(mHistory60day.size());
                        for (TransactionHistoryEntry entry: mHistory60day) uniqMap.put(entry.entryId, entry);

                        mHistory60day.clear();
                        mHistory60day.addAll(uniqMap.values());
                        dataGraphVPPercents();
                    }
                }
            }
        }.execute();
    }

    public void createPlot(ArrayList<String> dataPlotX, ArrayList<Integer> dataPlotY) {
        mChart.setOnChartGestureListener(new OnChartGestureListener() {

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
                                     float velocityY) {
                //Log.d("1", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
                int currVP = vpDash.getCurrentItem();
                if (velocityX < 0) {
                    Log.d("1", "Свайп влево");
                    if (!(currVP == 2)) {
                        vpDash.setCurrentItem(currVP + 1);
                    }
                } else {
                    Log.d("1", "Свайп вправо");
                    if (!(currVP == 0)) {
                        vpDash.setCurrentItem(currVP - 1);
                    }
                }
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
            }
        });

        //mChart.setUnit(" $");
        mChart.setDrawUnitsInChart(false);

        // if enabled, the chart will always start at zero on the y-axis
        mChart.setStartAtZero(false);

        // disable the drawing of values into the chart
        mChart.setDrawYValues(false);

        mChart.setDrawBorder(false);
//        mChart.setBorderPositions(new BorderPosition[] {
//                BorderPosition.BOTTOM
//        });

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawVerticalGrid(false);
        mChart.setDrawHorizontalGrid(false);
        mChart.setDrawLegend(false);
        mChart.setPadding(0, 0, 0, 0);
        //mChart.setFadingEdgeLength(20);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.BLACK);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // TO use for it
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);

        // set the marker TO the chart
        mChart.setMarkerView(mv);

        // add data
        //ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < dataPlotY.size(); i++) {
            //xVals.add(i + "");
            yVals.add(new Entry(dataPlotY.get(i), i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setColor(Color.parseColor("#ADFF2F"));
        //set1.setCircleColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(2f);
        //set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(117, 117, 117));
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.05f);


        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataPlotX, dataSets);

        // set data
        mChart.setData(data);

        mChart.animateXY(2000, 2000);

        //get the legend (only possible after setting data)
        //Legend l = mChart.getLegend();
        //l.setTextColor(Color.TRANSPARENT);
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
//        l.setForm(LegendForm.LINE);
//        l.setTextColor(Color.WHITE);
//
//        XLabels xl = mChart.getXLabels();
//        xl.setTextColor(Color.WHITE);
//
//        YLabels yl = mChart.getYLabels();
//        yl.setTextColor(Color.WHITE);

    }

    public void setPercent(String text) {
        if (text.isEmpty()) {
            tvPercent.setText("");
            ivPercent.setVisibility(View.INVISIBLE);
        } else if (text.startsWith("-")) {
            tvPercent.setText(text.replace("-", ""));
            tvPercent.setTextColor(Color.RED);
            ivPercent.setVisibility(View.VISIBLE);
            ivPercent.setImageResource(R.drawable.down);
        } else {
            tvPercent.setText(text);
            tvPercent.setTextColor(Color.parseColor("#9ACD32"));
            ivPercent.setVisibility(View.VISIBLE);
            ivPercent.setImageResource(R.drawable.up);
        }
    }

    //подготовка данных для графика, для ViewPager, для процентов
    void dataGraphVPPercents() {
        String[] dataPeriodElem;
        Date currentDate = new Date();
        float fSumDay = 0;
        float fSumWeek = 0;
        float fSumMonth = 0;
        float fSumDay2 = 0;
        float fSumWeek2 = 0;
        float fSumMonth2 = 0;
        int sum;
        long diffSecond;
        int diffHour;
        int diffDay;
        Calendar calendar;

        for (int i = 23; i > -1; i--) {
            calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
            calendar.add(Calendar.HOUR_OF_DAY, -i);
            dataPlotDay.add(0);
            dataPlotDayX.add(calendar.get(Calendar.HOUR_OF_DAY) + " " +
                    getString(R.string.hour_cut));
        }
        for (int i = 6; i > -1; i--) {
            calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            dataPlotWeek.add(0);
            dataPlotWeekX.add(getResources().getStringArray(R.array.day_of_week)
                    [calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        }
        for (int i = 29; i > -1; i--) {
            calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            dataPlotMonth.add(0);
            dataPlotMonthX.add(calendar.get(Calendar.DAY_OF_MONTH)
                    + " " + getResources().getStringArray(R.array.month_array_cut)
                    [calendar.get(Calendar.MONTH)] +
                    ", " + getResources().getStringArray(R.array.day_of_week)
                    [calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        }

        //за 24 часа и пред день
        for (TransactionHistoryEntry entry: mHistory60day) {
            diffSecond = Math.abs(currentDate.getTime() - entry.createDate.getTime()) / 1000;
            diffHour = (int) diffSecond / 3600;

            sum = entry.amount
                    .subtract(entry.commissionAmount)
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            if (diffHour < 24) {
                fSumDay += sum;
                dataPlotDay.set(23 - diffHour, dataPlotDay.get(23 - diffHour) + sum);
                //предыдущий день для процента
            } else if (diffHour < 48) {
                fSumDay2 += sum;
            }
        }

        // за неделю, месяц и пред
        for (TransactionHistoryEntry entry: mHistory60day) {
            sum = entry.amount
                    .subtract(entry.commissionAmount)
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .intValue();

            diffDay = (int)(Math.abs(currentDate.getTime() - entry.createDate.getTime()) / 86400000);

            if (diffDay < 7) {
                dataPlotWeek.set(6 - diffDay, dataPlotWeek.get(6 - diffDay) + sum);
                dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
                fSumWeek += sum;
            } else if (diffDay < 14) {
                fSumWeek2 += sum;
                dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
            } else if (diffDay < 30) {
                dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
                fSumMonth += sum;
            } else {
                fSumMonth2 += sum;
            }
        }

        fSumMonth += fSumWeek2;
        fSumMonth += fSumWeek;
        //Log.d("1", fSumDay + " " + fSumWeek + " " + fSumMonth);

        clearVPData();
        dataDayWeekMonth.add(TextUtilsW1.formatNumberNoFract(fSumDay + ""));
        dataDWMCurrency.add(TextUtilsW1.getCurrencySymbol(mListener.getCurrency()));
        dataDayWeekMonth.add(TextUtilsW1.formatNumberNoFract(fSumWeek + ""));
        dataDWMCurrency.add(TextUtilsW1.getCurrencySymbol(mListener.getCurrency()));
        dataDayWeekMonth.add(TextUtilsW1.formatNumberNoFract(fSumMonth + ""));
        dataDWMCurrency.add(TextUtilsW1.getCurrencySymbol(mListener.getCurrency()));

        if (fSumDay2 > 0) {
            percentDay = (int) (fSumDay / (fSumDay2 / 100) - 100) + " %";
        } else {
            percentDay = "";
        }

        if (fSumWeek2 > 0) {
            percentWeek = (int) (fSumWeek / (fSumWeek2 / 100) - 100) + " %";
        } else {
            percentWeek = "";
        }

        if (fSumMonth2 > 0) {
            percentMonth = (int) (fSumMonth / (fSumMonth2 / 100) - 100) + " %";
        } else {
            percentMonth = "";
        }

        setViewPager();
        createPlot(dataPlotDayX, dataPlotDay);
        setPercent(percentDay);
        currentPlot = DashFragment.PLOT_24;
    }

    private void clearVPData() {
        dataDayWeekMonth.clear();
        dataDWMCurrency.clear();
    }

    private Date getDate60DaysAgo() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_MONTH, -60);
        return calendar.getTime();
    }

    public class MyMarkerView extends MarkerView {

        private TextView tvContent, tvDate;
        MenuActivity menuActivity;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            menuActivity = (MenuActivity) context;
            tvContent = (TextView) findViewById(R.id.tvContent);
            tvDate = (TextView) findViewById(R.id.tvDate);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content
        @Override
        public void refreshContent(Entry e, int dataSetIndex) {

            if (e instanceof CandleEntry) {
                CandleEntry ce = (CandleEntry) e;
                tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
            } else {
                // define an offset to change the original position of the marker
                // (optional)

                setOffsets(-getMeasuredWidth() / 2, -getMeasuredHeight() / 2);
                int[] location = new int[2];
                getLocationInWindow(location);
                String amount = e.getVal() + "";
                tvContent.setText(TextUtilsW1.formatNumberNoFract(amount));
                if (currentPlot == DashFragment.PLOT_24) {
                    if (dataPlotDayX.size() > 0) {
                        tvDate.setText(dataPlotDayX.get(e.getXIndex()));
                    }
                } else if (currentPlot == DashFragment.PLOT_WEEK) {
                    if (dataPlotWeekX.size() > 0) {
                        tvDate.setText(dataPlotWeekX.get(e.getXIndex()));
                    }
                } else if (currentPlot == DashFragment.PLOT_30) {
                    if (dataPlotMonthX.size() > 0) {
                        tvDate.setText(dataPlotMonthX.get(e.getXIndex()));
                    }
                }
            }
        }
    }

    public interface OnFragmentInteractionListener {
        public String getCurrency();

        public void startProgress();

        public void stopProgress();

        public void onBalanceLoaded(List<Balance> balance);

    }
}
