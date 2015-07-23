package com.w1.merchant.android.ui.withdraw;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.rest.model.ProviderList;
import com.w1.merchant.android.ui.adapter.CurrencyAdapter;
import com.w1.merchant.android.ui.adapter.WithdrawalGridAdapter;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

/**
 * Список провайдеров
 */
public class ProviderListFragment extends Fragment {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String KEY_CURRENCY_LIST = "com.w1.merchant.android.ui.withdraw.ProviderListFragment.ProviderListFragment.KEY_CURRENCY_LIST";

    private OnFragmentInteractionListener mListener;

    private Subscription mCurrencyListSubscription = Subscriptions.unsubscribed();

    private RecyclerView mRecyclerView;
    private View mNoItemsTextView;

    private Spinner mCurrencySpinner;

    private View mProgressBar;

    private WithdrawalGridAdapter mAdapter;
    private RefreshStateOnAdapterChangeObserver mOnRefreshObserver;

    private BaseAdapter mCurrencyAdapter;

    private ArrayList<String> mCurrencyList;

    private ProviderListFragmentData mData;

    public ProviderListFragment() {
        // Required empty public constructor
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrencyList = savedInstanceState.getStringArrayList(KEY_CURRENCY_LIST);
            if (mCurrencyList == null) mCurrencyList = new ArrayList<>();
        } else {
            mCurrencyList = new ArrayList<>();
        }
        mData = new ProviderListFragmentData(this, new ProviderListFragmentData.InteractionListener() {
            @Override
            public void onLoadingStateChanged() {
                setupLoadingState();
            }

            @Override
            public void onKeepOnAppendingChanged(boolean newValue) {}

            @Override
            public void onError(CharSequence description, Throwable e) {
                showError(description, e);
            }

            @Override
            public void onProviderListChanged(ProviderList providerList) {
                if (mAdapter == null) return;
                String selectedCurrencyId = getSelectedCurrencyId();
                if (selectedCurrencyId == null) return;

                SortedList<Provider> adapterList = mAdapter.getList();
                adapterList.beginBatchedUpdates();
                try {
                    for (Provider provider : providerList.providers) {
                        if (selectedCurrencyId.equals(provider.currencyId)) {
                            adapterList.add(provider);
                        }
                    }
                }finally {
                    adapterList.endBatchedUpdates();
                }
            }
        });
        mData.onCreate(savedInstanceState
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider_list, container, false);
        mRecyclerView = (RecyclerView)root.findViewById(R.id.recycler_view);
        mNoItemsTextView = root.findViewById(R.id.empty_text);
        mProgressBar = root.findViewById(R.id.ab2_progress);
        mCurrencySpinner = (Spinner)root.findViewById(R.id.spinner);

        setupRecyclerView();
        initCurrencySpinner();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            int abOptions =  ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP;
            actionBar.setDisplayOptions(abOptions, abOptions);
        }

        mAdapter = new WithdrawalGridAdapter(getActivity()) {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
                if (viewType == WithdrawalGridAdapter.VIEW_TYPE_GRID_ITEM) {
                    holder.itemView.setOnClickListener(mOnClickListener);
                }
                return holder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                mData.onBindViewHolder(position, getList().size());
            }

            private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRecyclerView == null || mListener == null) return;
                    int position = mRecyclerView.getChildAdapterPosition(v);
                    if (position != RecyclerView.NO_POSITION && !isPendingIndicatorPosition(position)) {
                        Provider provider = getList().get(position);
                        if (DBG) Log.v(TAG,"onProviderClicked " + provider.providerId);
                        if (mListener != null) mListener.onProviderClicked((ViewHolderItem) mRecyclerView.getChildViewHolder(v), provider);
                    }
                }
            };
        };
        mOnRefreshObserver = new RefreshStateOnAdapterChangeObserver();
        mAdapter.registerAdapterDataObserver(mOnRefreshObserver);
        mRecyclerView.setAdapter(mAdapter);

        if (mCurrencyList.isEmpty()) refreshCurrencyList();
        mData.reloadProviderList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mCurrencyList.isEmpty()) outState.putStringArrayList(KEY_CURRENCY_LIST, mCurrencyList);
        mData.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mData.onDestroy();
        mCurrencyListSubscription.unsubscribe();
        mAdapter.unregisterAdapterDataObserver(mOnRefreshObserver);
        mOnRefreshObserver.mHandler.removeCallbacksAndMessages(null);
        mOnRefreshObserver = null;
        mAdapter = null;
        mRecyclerView = null;
        mNoItemsTextView = null;
        mProgressBar = null;
        mCurrencySpinner = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initCurrencySpinner() {
        mCurrencyAdapter = new CurrencyAdapter(getActivity(), mCurrencyList);
        mCurrencySpinner.setAdapter(mCurrencyAdapter);
        mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (DBG) Log.v(TAG, "on currency selected " + parent.getItemAtPosition(position));
                refreshProviderAdapterList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        mRecyclerView.getItemAnimator().setAddDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        mRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);

        final int numSpans = getResources().getInteger(R.integer.withdrawal_grid_span_count);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numSpans);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                if (mAdapter != null && mAdapter.isPendingIndicatorPosition(position)) {
                    return numSpans;
                } else {
                    return 1;
                }
            }

            @Override
            public int getSpanIndex(int position, int spanCount) {
                if (mAdapter != null && mAdapter.isPendingIndicatorPosition(position)) {
                    return 0;
                } else {
                    return position % spanCount;
                }
            }
        };

        layoutManager.setSpanSizeLookup(spanSizeLookup);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupLoadingState() {
        if (mRecyclerView == null || mAdapter == null) return;

        boolean isLoading = mData.isLoading() || !mCurrencyListSubscription.isUnsubscribed();
        boolean isRefreshing = mData.isAppendActive();
        boolean showNoItems = !isLoading && !isRefreshing && mAdapter.getList().size() == 0;

        mNoItemsTextView.setVisibility(showNoItems ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        mAdapter.setShowLoadMoreButton(isRefreshing);
    }

    @Nullable
    private String getSelectedCurrencyId() {
        return mCurrencySpinner == null ? null : (String)mCurrencySpinner.getSelectedItem();
    }

    private void refreshProviderAdapterList() {
        if (mAdapter == null) return;
        String selectedCurrencyId = getSelectedCurrencyId();
        if (selectedCurrencyId == null) return;

        SortedList<Provider> list = mAdapter.getList();
        refreshProviderAdapterList(mData.getProviderList(), selectedCurrencyId, list);
    }

    private static void refreshProviderAdapterList(Collection<Provider> providers,
                                            String selectedCurrencyId,
                                            SortedList<Provider> adapterList
                                            ) {
        adapterList.beginBatchedUpdates();
        try {
            for (int i = adapterList.size() - 1; i >= 0; --i) {
                if (!selectedCurrencyId.equals(adapterList.get(i).providerId)) adapterList.removeItemAt(i);
            }
            for (Provider provider : providers) {
                if (selectedCurrencyId.equals(provider.currencyId)) {
                    adapterList.add(provider);
                }
            }
        }finally {
            adapterList.endBatchedUpdates();
        }
    }

    private void showError(CharSequence defaultErrorDescription, Throwable throwable) {
        CharSequence errText;
        if (throwable instanceof ResponseErrorException) {
            errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
        } else {
            errText = defaultErrorDescription;
        }
        Snackbar.make(getView(), errText, Snackbar.LENGTH_LONG).show();
    }

    private void refreshCurrencyList() {
        if (!mCurrencyListSubscription.isUnsubscribed()) return;

        rx.Observable<List<Balance>> observable = RestClient.getApiBalance().getBalance();
        mCurrencyListSubscription = AppObservable.bindFragment(this, observable)
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .subscribe(mCurrencyListObserver);
    }

    private final Observer<List<Balance>> mCurrencyListObserver = new Observer<List<Balance>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            showError(getText(R.string.network_error), e);
        }

        @Override
        public void onNext(List<Balance> balances) {
            if (mCurrencySpinner == null) return;
            String oldSelectedCurrencyId = getSelectedCurrencyId();

            mCurrencyList.clear();
            for (Balance balance: balances) mCurrencyList.add(balance.currencyId);
            mCurrencyAdapter.notifyDataSetChanged();

            int newCurrencyPos = -1;
            if (oldSelectedCurrencyId != null) newCurrencyPos = mCurrencyList.indexOf(oldSelectedCurrencyId);
            if (newCurrencyPos < 0) {
                Balance balance = findNativeBalance(balances);
                if (balance != null) newCurrencyPos = mCurrencyList.indexOf(balance.currencyId);
            }
            if (newCurrencyPos >= 0) {
                mCurrencySpinner.setSelection(newCurrencyPos);
            }
        }
    };

    @Nullable
    private Balance findNativeBalance(List<Balance> list) {
        for (Balance balance: list) {
            if (balance.isNative) {
                return balance;
            }
        }
        return null;
    }

    private class RefreshStateOnAdapterChangeObserver extends RecyclerView.AdapterDataObserver {

        private final Handler mHandler;

        public RefreshStateOnAdapterChangeObserver() {
            super();
            mHandler = new Handler();
        }

        private Runnable mRefreshStateList = new Runnable() {
            @Override
            public void run() {
                setupLoadingState();
            }
        };

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            mHandler.postDelayed(mRefreshStateList, 5 * 16);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            mHandler.postDelayed(mRefreshStateList, 5 * 16);
        }
    };

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
    public interface OnFragmentInteractionListener {
        void onProviderClicked(WithdrawalGridAdapter.ViewHolderItem holder, Provider provider);
    }

}
