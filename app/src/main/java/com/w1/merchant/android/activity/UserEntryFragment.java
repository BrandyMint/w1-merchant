package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.UserEntryAdapter2;
import com.w1.merchant.android.model.TransactionHistory;
import com.w1.merchant.android.model.TransactionHistoryEntry;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

import java.util.Calendar;

import retrofit.Callback;
import retrofit.client.Response;

public class UserEntryFragment extends Fragment {

    private static final int ITEMS_PER_PAGE = 25;

    private ListView lvUserEntry;
    private TextView mFooter;
    private SegmentedRadioGroup mRadioGroup;

    private OnFragmentInteractionListener mListener;

    private String mSearchString;

    private ApiUserEntry mApiUserEntry;

    private UserEntryAdapter2 mAdapter;

    private int mCurrentPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);
        setHasOptionsMenu(true);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.userentry, container, false);
        lvUserEntry = (ListView) parentView.findViewById(R.id.lvStatement);
        mRadioGroup = (SegmentedRadioGroup) parentView.findViewById(R.id.srgStatement);
        mFooter = (TextView)inflater.inflate(R.layout.footer2, lvUserEntry, false);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                hideFooter();
                mCurrentPage = 1;
                refreshList();
            }
        });

        mAdapter = new UserEntryAdapter2(getActivity());
        mFooter.setVisibility(View.GONE);
        lvUserEntry.addFooterView(mFooter, null, false);
        lvUserEntry.setAdapter(mAdapter);

        lvUserEntry.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                UserEntryAdapter2.ViewHolder holder = (UserEntryAdapter2.ViewHolder)view.getTag(R.id.tag_transaction_history_view_holder);
                TransactionHistoryEntry entry = (TransactionHistoryEntry)parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), Details.class);
                intent.putExtra("number", holder.name.getText().toString());
                intent.putExtra("date", holder.date.getText().toString());
                intent.putExtra("descr", entry.description);
                intent.putExtra("amount", holder.amount0);
                intent.putExtra("currency", String.valueOf(entry.currencyId));

                int stateRes;
                if (entry.isAccepted()) {
                    stateRes = R.string.paid;
                } else if (entry.isCanceled() || entry.isRejected()) {
                    stateRes = R.string.canceled;
                } else {
                    stateRes = R.string.processing;
                }
                intent.putExtra("state", view.getResources().getString(stateRes));

                startActivity(intent);
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
        SearchView searchView = (SearchView) menu.findItem(
                R.id.ic_menu_search0).getActionView();
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
        mListener.startProgress();
        new ApiRequestTask<TransactionHistory>() {

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {
                final String direction;
                if (mRadioGroup == null) return;

                switch (mRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.rbEntrance:
                        direction = TransactionHistoryEntry.DIRECTION_INCOMING;
                        break;
                    case R.id.rbOutput:
                        direction = TransactionHistoryEntry.DIRECTION_OUTCOMING;
                        break;
                    default:
                        direction = null;
                        break;
                }

                mApiUserEntry.getEntries(mCurrentPage, ITEMS_PER_PAGE,
                        null, null, null, null,
                        mListener.getCurrency(),
                        mSearchString, direction, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return UserEntryFragment.this.getActivity();
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
                    addUserEntry(transactionHistory);
                }
            }
        }.execute();
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

                        UserEntryTotal.startActivity(context, from.getTime(), to.getTime(), nativeCurrency);
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

    public interface OnFragmentInteractionListener {
        public String getCurrency();

        public void startProgress();

        public void stopProgress();

        public View getPopupAnchorView();
    }


}
