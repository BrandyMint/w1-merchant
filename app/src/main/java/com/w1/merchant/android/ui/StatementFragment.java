package com.w1.merchant.android.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.TransactionHistory;
import com.w1.merchant.android.rest.model.TransactionHistoryEntry;
import com.w1.merchant.android.ui.adapter.TransactionHistoryAdapter;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.ui.widget.CheckboxStyleSegmentedRadioGroup;

import java.util.Calendar;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

/**
 * Выписка
 */
public class StatementFragment extends Fragment {

    private static final int ITEMS_PER_PAGE = 25;

    private ListView lvUserEntry;
    private TextView mFooter;
    private CheckboxStyleSegmentedRadioGroup mRadioGroup;

    private OnFragmentInteractionListener mListener;

    private String mSearchString;

    private TransactionHistoryAdapter mAdapter;

    private int mCurrentPage = 1;

    private Subscription mTransactionHistorySubscription = Subscriptions.empty();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_statement, container, false);
        lvUserEntry = (ListView) parentView.findViewById(R.id.lvStatement);
        mRadioGroup = (CheckboxStyleSegmentedRadioGroup) parentView.findViewById(R.id.srgStatement);
        mFooter = (TextView)inflater.inflate(R.layout.footer2, lvUserEntry, false);

        mRadioGroup.setOnCheckedChangeListener(new CheckboxStyleSegmentedRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckboxStyleSegmentedRadioGroup group, int checkedId) {
                hideFooter();
                mCurrentPage = 1;
                refreshList();
            }
        });

        mAdapter = new TransactionHistoryAdapter(getActivity());
        mFooter.setVisibility(View.GONE);
        lvUserEntry.addFooterView(mFooter, null, false);
        lvUserEntry.setAdapter(mAdapter);

        lvUserEntry.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                TransactionHistoryEntry entry = (TransactionHistoryEntry)parent.getItemAtPosition(position);
                DetailsActivity.startActivity(getActivity(), entry, view);
            }
        });

        lvUserEntry.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView arg0,
                                 int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // when list scrolling stops
                        manipulateWithVisibleViews(view);
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }
        });

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.transaction_history, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(
                R.id.ic_menu_search0));
        searchView.setQueryHint(getText(R.string.search_transaction_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mListener != null) {
                    mCurrentPage = 1;
                    setSearchString(newText);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (mListener != null) {
                    mCurrentPage = 1;
                    setSearchString(null);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_menu_add0:
                showCalendarPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTransactionHistorySubscription.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void showCalendarPopup() {
        new SelectDateRangePopup(getActivity(), mListener.getCurrency())
                .show(mListener.getPopupAnchorView());
    }

    public void setSearchString(String mSearchString) {
        this.mSearchString = TextUtils.isEmpty(mSearchString) ? null : mSearchString;
        refreshList();
    }

    private void refreshList() {
        mTransactionHistorySubscription.unsubscribe();

        if (mRadioGroup == null) return;

        final String direction;
        switch (mRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rbEntrance:
                direction = TransactionHistoryEntry.DIRECTION_INCOMING;
                break;
            case R.id.rbOutput:
                direction = TransactionHistoryEntry.DIRECTION_OUTGOING;
                break;
            default:
                direction = null;
                break;
        }

        Observable<TransactionHistory> observable = AppObservable.bindFragment(this,
                RestClient.getApiUserEntry().getEntries(mCurrentPage, ITEMS_PER_PAGE,
                        null, null, null, null,
                        mListener.getCurrency(),
                        mSearchString, direction));

        NetworkUtils.StopProgressAction action0 = new NetworkUtils.StopProgressAction(mListener);
        mTransactionHistorySubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(action0)
                .subscribe(new Observer<TransactionHistory>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (getActivity() != null) {
                            CharSequence errText = ((ResponseErrorException) e).getErrorDescription(getText(R.string.network_error), getResources());
                            Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNext(TransactionHistory transactionHistory) {
                        addUserEntry(transactionHistory);
                    }
                });
        action0.token = mListener.startProgress();
    }

    private void addUserEntry(TransactionHistory newData) {
        if (!(newData == null)) {
            if (mCurrentPage == 1) {
                mAdapter.setItems(newData.items);
                if (mAdapter.getCount() >= ITEMS_PER_PAGE) {
                    showFooter();
                } else {
                    hideFooter();
                }
            } else {
                mAdapter.addItems(newData.items);
                setHeaderText(getString(R.string.data_load));
            }
            if (newData.items.size() == 0) hideFooter();
        } else {
            hideFooter();
        }
        if (getView() != null) {
            getView().findViewById(R.id.empty_text).setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void manipulateWithVisibleViews(AbsListView view) {
        if (view.getLastVisiblePosition() == mAdapter.getCount()) {
            mCurrentPage += 1;
            mFooter.setText(R.string.loading);
            refreshList();
        }
    }

    static class SelectDateRangePopup {
        DatePicker dp1;
        TextView tvBack;
        TextView tvDate;
        int current = 0;
        int day0, month0, year0;
        int day1, month1, year1;

        final PopupWindow popupWindow;

        public SelectDateRangePopup(final Context context, final String nativeCurrency) {
            //итоги по выписке
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            Calendar today = Calendar.getInstance();
            day1 = today.get(Calendar.DAY_OF_MONTH);
            month1 = today.get(Calendar.MONTH);
            year1 = today.get(Calendar.YEAR);

            today.add(Calendar.MONTH, -1);
            day0 = today.get(Calendar.DAY_OF_MONTH);
            month0 = today.get(Calendar.MONTH);
            year0 = today.get(Calendar.YEAR);

            @SuppressLint("InflateParams") View popupView = layoutInflater.inflate(R.layout.popup_date, null);
            popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dp1 = (DatePicker) popupView.findViewById(R.id.dp1);
            dp1.init(year0, month0, day0, null);

            tvBack = (TextView) popupView.findViewById(R.id.tvBack);
            tvDate = (TextView) popupView.findViewById(R.id.tvDate);
            TextView tvNext = (TextView) popupView.findViewById(R.id.tvNext);

            tvNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (current == 0) {
                        tvBack.setText(R.string.go_back);
                        tvDate.setText(R.string.end_date);
                        current += 1;
                        day0 = dp1.getDayOfMonth();
                        month0 = dp1.getMonth();
                        year0 = dp1.getYear();
                        dp1.init(year1, month1, day1, null);
                    } else {
                        //запускаем итоги по выписке
                        current = 0;
                        popupWindow.dismiss();
                        day1 = dp1.getDayOfMonth();
                        month1 = dp1.getMonth();
                        year1 = dp1.getYear();

                        Calendar from = Calendar.getInstance();
                        from.set(year0, month0, day0, 0, 0, 0);

                        Calendar to = Calendar.getInstance();
                        to.set(year1, month1, day1, 0, 0, 0);

                        TranscationSummaryReportActivity.startActivity(context, from.getTime(), to.getTime(), nativeCurrency);
                    }
                }
            });

            tvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (current == 0) {
                        //выходим
                        current = 0;
                        popupWindow.dismiss();
                    } else {
                        tvBack.setText(R.string.cancel);
                        tvDate.setText(R.string.begin_date);
                        dp1.init(year0, month0, day0, null);
                        current -= 1;
                    }
                }
            });

        }

        public void show(View anchor) {
            popupWindow.showAsDropDown(anchor, 0, 0);
        }
    }

    void hideFooter() {
        if (mFooter != null) mFooter.setVisibility(View.GONE);
    }

    void showFooter() {
        if (mFooter != null) mFooter.setVisibility(View.VISIBLE);
    }

    public void setHeaderText(String text) {
        mFooter.setText(text);
    }

    public interface OnFragmentInteractionListener extends IProgressbarProvider {
        public String getCurrency();

        public View getPopupAnchorView();
    }


}
