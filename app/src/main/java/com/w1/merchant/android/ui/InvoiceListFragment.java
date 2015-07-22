package com.w1.merchant.android.ui;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Invoice;
import com.w1.merchant.android.rest.model.Invoices;
import com.w1.merchant.android.ui.adapter.InvoiceListAdapter;
import com.w1.merchant.android.ui.widget.CheckboxStyleSegmentedRadioGroup;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class InvoiceListFragment extends Fragment {

    public static final int ITEMS_PER_PAGE = 25;

    private CheckboxStyleSegmentedRadioGroup srgInvoice;
    private TextView llFooter;

    private InvoiceListAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    private int mCurrentPage = 1;

    private String mSearchString;

    private Subscription mGetInvoicesSubscription = Subscriptions.empty();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_invoice_list, container, false);
        srgInvoice = (CheckboxStyleSegmentedRadioGroup) parentView.findViewById(R.id.srgInvoice);
        ListView lvInvoice = (ListView) parentView.findViewById(R.id.lvAccounts);
        llFooter = (TextView)inflater.inflate(R.layout.footer2, lvInvoice, false);
        
        srgInvoice.setOnCheckedChangeListener(new CheckboxStyleSegmentedRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckboxStyleSegmentedRadioGroup group, int checkedId) {
                hideFooter();
                mCurrentPage = 1;
                refreshList();
            }
        });

        mAdapter = new InvoiceListAdapter(getActivity());
        llFooter.setVisibility(View.GONE);
        lvInvoice.addFooterView(llFooter, null, false);
        lvInvoice.setAdapter(mAdapter);

        lvInvoice.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                }
            }
        });

        lvInvoice.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Invoice entry = (Invoice) parent.getItemAtPosition(position);
                DetailsActivity.startActivity(getActivity(), entry, view);

            }
        });
        lvInvoice.setTextFilterEnabled(true);

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
        inflater.inflate(R.menu.invoices_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(
                R.id.ic_menu_search0));
        searchView.setQueryHint(getText(R.string.search_invoice_hint));
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
                //добавления счета
                Intent intent = new Intent(getActivity(), AddInvoiceActivity.class);
                startActivity(intent);
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
        mGetInvoicesSubscription.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setSearchString(String searchString) {
        this.mSearchString = TextUtils.isEmpty(searchString) ? null : searchString;
        refreshList();
    }

    private void refreshList() {
        mGetInvoicesSubscription.unsubscribe();

        final String invoiceStateId;
        switch (srgInvoice.getCheckedRadioButtonId()) {
            case R.id.rbPaid:
                invoiceStateId = Invoice.STATE_ACCEPTED;
                break;
            case R.id.rbNotPaid:
                invoiceStateId = Invoice.STATE_CREATED;
                break;
            case R.id.rbPartially:
                invoiceStateId = Invoice.STATE_CREATED;
                break;
            default:
                invoiceStateId = null;
                break;
        }

        Observable<Invoices> observable = AppObservable.bindFragment(this,
                RestClient.getApiInvoices().getInvoices(mCurrentPage,
                        ITEMS_PER_PAGE, invoiceStateId, null, null, null, mSearchString));

        NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);
        mGetInvoicesSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(stopProgressAction)
                .subscribe(new Observer<Invoices>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null) {
                            CharSequence errText = ((ResponseErrorException)e).getErrorDescription(getText(R.string.network_error), getResources());
                            Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNext(Invoices invoices) {
                        addInvoices(invoices);
                    }
                });
        stopProgressAction.token = mListener.startProgress();
    }

    private void addInvoices(Invoices newData) {
        if (newData != null) {
            List<Invoice> invoices;
            if (srgInvoice.getCheckedRadioButtonId() == R.id.rbPartially) {
                invoices = new ArrayList<>(newData.invoices.size());
                for (Invoice invoice: newData.invoices) {
                    if (invoice.isPartiallyPaid()) invoices.add(invoice);
                }
            } else {
                invoices = newData.invoices;
            }

            if (mCurrentPage == 1) {
                mAdapter.setItems(invoices);
                if (mAdapter.getCount() >= ITEMS_PER_PAGE) {
                    showFooter();
                } else {
                    hideFooter();
                }
            } else {
                mAdapter.addItems(invoices);
                llFooter.setText(R.string.data_load);
            }
            if (invoices.size() == 0) hideFooter();
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
            llFooter.setText(R.string.loading);
            refreshList();
        }
    }

    public void showFooter() {
        if (llFooter != null) llFooter.setVisibility(View.VISIBLE);
    }

    public void hideFooter() {
        if (llFooter != null) llFooter.setVisibility(View.GONE);
    }

    public interface OnFragmentInteractionListener extends IProgressbarProvider {
    }
}
