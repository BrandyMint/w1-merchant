package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.graphs.DayGraphFragment;
import com.w1.merchant.android.activity.graphs.WeekMonthGraphFragment;
import com.w1.merchant.android.extra.UserEntryAdapter2;
import com.w1.merchant.android.model.Balance;
import com.w1.merchant.android.model.TransactionHistory;
import com.w1.merchant.android.model.TransactionHistoryEntry;
import com.w1.merchant.android.service.ApiBalance;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class DashFragment extends Fragment  implements
        DayGraphFragment.OnFragmentInteractionListener,
        WeekMonthGraphFragment.OnFragmentInteractionListener{
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;
    private static final int TRANSACTION_HISTORY_ITEMS_PER_PAGE = 25;
    private ListView lvDash;

    private ViewPager vpDash;
    private GraphicsAdapter mPagerAdapter;
    private SegmentedRadioGroup srgDash;
    private RadioButton rbHour;
    private RadioButton rbWeek;
    private RadioButton rbMonth;
    private TextView mFooter;
    private View llHeader;
    //XYSeries series1;
    private SwipeRefreshLayout swipeLayout;

    private int mCurrentPage = 1;

    private OnFragmentInteractionListener mListener;

    private UserEntryAdapter2 mAdapter;
    private ApiUserEntry mApiUserEntry;
    private ApiBalance mApiBalance;

    private List<Fragment> mFragments = new ArrayList<>(3);

    private boolean mCurrencyLoaded;

    private Subscription mRefreshBalanceSubscription = Subscriptions.empty();
    private Subscription mLoadTransactionsSubscription = Subscriptions.empty();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);
        mApiBalance = NetworkUtils.getInstance().createRestAdapter().create(ApiBalance.class);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.dashboard, container, false);
        //llPlot = (LinearLayout) llHeader.findViewById(R.id.llPlot);
        lvDash = (ListView) parentView.findViewById(R.id.lvDash);

        mFooter = (TextView)inflater.inflate(R.layout.footer2, lvDash, false);
        llHeader = inflater.inflate(R.layout.dash_header, lvDash, false);

        srgDash = (SegmentedRadioGroup) llHeader.findViewById(R.id.srgDash);
        rbHour = (RadioButton) llHeader.findViewById(R.id.rbHour);
        rbHour.setChecked(true);
        rbWeek = (RadioButton) llHeader.findViewById(R.id.rbWeek);
        rbMonth = (RadioButton) llHeader.findViewById(R.id.rbMonth);
        swipeLayout = (SwipeRefreshLayout) parentView;
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDashboard();
            }
        });
        mAdapter = new UserEntryAdapter2(getActivity());
        setupListView();
        setupViewPager();

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshDashboard();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRefreshBalanceSubscription.unsubscribe();
        mLoadTransactionsSubscription.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshDashboard() {
        mCurrentPage = 1;
        refreshBalance();
    }

    @Override
    public void onFragmentAttached(Fragment fragment) {
        mFragments.add(fragment);
    }

    @Override
    public void onFragmentDetached(Fragment fragment) {
        mFragments.remove(fragment);
    }

    @Override
    public Object startProgress() {
        if (mListener != null) {
            return mListener.startProgress();
        } else {
            return new Object();
        }
    }

    @Override
    public void stopProgress(Object token) {
        if (mListener != null) mListener.stopProgress(token);
    }

    @Override
    public String getCurrency() {
        if (mListener == null) {
            return "643";
        } else {
            return mListener.getCurrency();
        }
    }

    @Override
    public boolean isCurrencyLoaded() {
        return mCurrencyLoaded;
    }

    private void setupListView() {
        hideFooter();
        lvDash.addHeaderView(llHeader);
        lvDash.addFooterView(mFooter);
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
            private final Rect scrollBounds = new Rect();

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

    void showFooter() {
        if (mFooter != null) mFooter.setVisibility(View.VISIBLE);
    }

    void hideFooter() {
        if (mFooter != null) mFooter.setVisibility(View.GONE);
    }

    void refreshTransactionHistory() {
        mLoadTransactionsSubscription.unsubscribe();

        Observable<TransactionHistory> observable = AppObservable.bindFragment(this,
                mApiUserEntry.getEntries(mCurrentPage, TRANSACTION_HISTORY_ITEMS_PER_PAGE,
                        null, null, null, null,
                        mListener.getCurrency(),
                        null, null));

        NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);
        mLoadTransactionsSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(stopProgressAction)
                .subscribe(new Observer<TransactionHistory>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (getActivity() != null) {
                            CharSequence errText = ((NetworkUtils.ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                            Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNext(TransactionHistory transactionHistory) {
                        addTransactionHistoryResult(transactionHistory);
                    }
                });
        stopProgressAction.token = mListener.startProgress();
    }

    private void addTransactionHistoryResult(TransactionHistory newData) {
        if (newData != null) {
            if (mCurrentPage == 1) {
                mAdapter.setItems(newData.items);
                swipeLayout.setRefreshing(false);
                if (mAdapter.getCount() >= TRANSACTION_HISTORY_ITEMS_PER_PAGE) {
                    showFooter();
                } else {
                    hideFooter();
                }
            } else {
                mAdapter.addItems(newData.items);
                mFooter.setText(R.string.data_load);
            }
            if (newData.items.size() == 0) hideFooter();
        } else {
            hideFooter();
        }
        if (getView() != null && llHeader != null) {
            llHeader.findViewById(R.id.empty_text).setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void setupViewPager() {
        srgDash.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbHour:
                        vpDash.setCurrentItem(0, true);
                        break;
                    case R.id.rbWeek:
                        vpDash.setCurrentItem(1);
                        break;
                    case R.id.rbMonth:
                        vpDash.setCurrentItem(2);
                        break;
                    default:
                        break;
                }
            }
        });

        vpDash = (ViewPager) llHeader.findViewById(R.id.vpDash);
        mPagerAdapter = new GraphicsAdapter(getChildFragmentManager());
        vpDash.setAdapter(mPagerAdapter);
        vpDash.setOffscreenPageLimit(4);
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
        mRefreshBalanceSubscription.unsubscribe();

        NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);
        Observable<List<Balance>> observer = AppObservable.bindFragment(this,
                mApiBalance.getBalance());
        mRefreshBalanceSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(stopProgressAction)
                .subscribe(new Observer<List<Balance>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        CharSequence errText = ((NetworkUtils.ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(List<Balance> balances) {
                        if (mListener != null) {
                            mCurrencyLoaded = true;
                            mListener.onBalanceLoaded(balances);
                            refreshTransactionHistory();
                            refreshGraphFragments();
                        }
                    }
                });
        stopProgressAction.token = mListener.startProgress();
    }

    private void refreshGraphFragments() {
        if (DBG) Log.v(TAG, "DashFragment: refreshGraphFragments");
        for (Fragment fragment: mFragments) {
            if (fragment instanceof  DayGraphFragment) {
                ((DayGraphFragment) fragment).reloadData();
            } else if (fragment instanceof WeekMonthGraphFragment) {
                ((WeekMonthGraphFragment) fragment).reloadData();
            }
        }
    }

    private static class GraphicsAdapter extends FragmentPagerAdapter {

        public GraphicsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DayGraphFragment.newInstance();
                case 1:
                    return WeekMonthGraphFragment.newInstanceWeekStats();
                case 2:
                    return WeekMonthGraphFragment.newInstanceMonthStats();
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public interface OnFragmentInteractionListener extends IProgressbarProvider {
        public String getCurrency();

        public void onBalanceLoaded(List<Balance> balance);

    }
}
