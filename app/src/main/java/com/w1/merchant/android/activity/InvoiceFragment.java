package com.w1.merchant.android.activity;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.InvoicesAdapter;
import com.w1.merchant.android.model.Invoice;
import com.w1.merchant.android.model.Invoices;
import com.w1.merchant.android.service.ApiInvoices;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;

public class InvoiceFragment extends Fragment {

    public static final int ITEMS_PER_PAGE = 25;
    private static final int ACT_ADD = 1;

    SegmentedRadioGroup srgInvoice;
	private TextView llFooter;

    private ApiInvoices mApiInvoices;

    private InvoicesAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    private int mCurrentPage = 1;

    private String mSearchString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiInvoices = NetworkUtils.getInstance().createRestAdapter().create(ApiInvoices.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //подтверждение добавления счета
        if (requestCode == ACT_ADD) {
            if (resultCode == Activity.RESULT_OK) {
                Intent confirmIntent = new Intent(getActivity().getApplicationContext(),
                        ConfirmActivity.class);
                startActivity(confirmIntent);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.invoices, container, false);
        srgInvoice = (SegmentedRadioGroup) parentView.findViewById(R.id.srgInvoice);
        ListView lvInvoice = (ListView) parentView.findViewById(R.id.lvAccounts);
        llFooter = (TextView)inflater.inflate(R.layout.footer2, lvInvoice, false);
        
        srgInvoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    		@Override
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			hideFooter();
                mCurrentPage = 1;
    			refreshList();
    		}
    	});

        mAdapter = new InvoicesAdapter(getActivity());
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
                Details.startActivity(getActivity(), entry, view);

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
                //добавления счета
                Intent intent = new Intent(getActivity(), AddInvoice.class);
                intent.putExtra("token", Session.getInstance().getBearer());
                startActivityForResult(intent, ACT_ADD);
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

    public void setSearchString(String searchString) {
        this.mSearchString = TextUtils.isEmpty(searchString) ? null : searchString;
        refreshList();
    }

    private void refreshList() {
        mListener.startProgress();

        new ApiRequestTask<Invoices>() {

            @Override
            protected void doRequest(Callback<Invoices> callback) {
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
                mApiInvoices.getInvoices(mCurrentPage,
                        ITEMS_PER_PAGE, invoiceStateId, null, null, null, mSearchString, callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return InvoiceFragment.this.getActivity();
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
                if (mListener != null) {
                    mListener.stopProgress();
                }
            }

            @Override
            protected void onSuccess(Invoices invoices, Response response) {
                if (mListener != null) {
                    mListener.stopProgress();
                    addUserEntry(invoices);
                }
            }
        }.execute();
    }

    private void addUserEntry(Invoices newData) {
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

    public interface OnFragmentInteractionListener {
        public String getCurrency();

        public void startProgress();

        public void stopProgress();

    }
}
