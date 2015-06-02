package com.w1.merchant.android.support;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.SupportTickets;
import com.w1.merchant.android.service.ApiSupport;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.viewextended.DividerItemDecoration;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TicketListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TicketListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TicketListFragment extends Fragment {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String BUNDLE_KEY_SUPPORT_TICKETS = "com.w1.merchant.android.support.BUNDLE_KEY_SUPPORT_TICKETS";

    private static final int REFRESH_DATES_PERIOD = 10;

    private OnFragmentInteractionListener mListener;

    private ApiSupport mApiSupport;

    private RecyclerView mListView;
    private View mAdapterEmptyView;
    private View mProgressView;

    private TicketListAdapter mAdapter;

    private Subscription mRefreshConversationsSubscription = Subscriptions.unsubscribed();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TicketListFragment.
     */
    public static TicketListFragment newInstance() {
        return  new TicketListFragment();
    }

    public TicketListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiSupport = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ticket_list, container, false);
        mListView = (RecyclerView)root.findViewById(R.id.list);
        mAdapterEmptyView = root.findViewById(R.id.empty_text);
        mProgressView = root.findViewById(R.id.progress);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(lm);
        mListView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.list_divider));
        mListView.getItemAnimator().setAddDuration(getResources().getInteger(android.R.integer.config_longAnimTime));

        mAdapter = new TicketListAdapter() {
            @Override
            public void initClickListeners(ViewHolder holder) {
                holder.itemView.setOnClickListener(mOnClickListener);
            }

            final View.OnClickListener mOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) mListener.onOpenConversationClicked(v,
                            mAdapter.getItem(mListView.getChildPosition(v)));
                }
            };
        };

        if (savedInstanceState != null) {
            List<SupportTicket> tickets = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_SUPPORT_TICKETS);
            if (tickets != null) mAdapter.setTickets(tickets);
        }

        mListView.setAdapter(mAdapter);

        return root;
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
    public void onResume() {
        super.onResume();
        refreshConversationList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mAdapter.getTickets().isEmpty()) outState.putParcelableArrayList(BUNDLE_KEY_SUPPORT_TICKETS, mAdapter.getTickets());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRefreshConversationsSubscription.unsubscribe();
        mListView = null;
        mAdapterEmptyView = null;
        mProgressView = null;
        mAdapter = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_ticket_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_start_conversation:
                if (mListener != null) mListener.onStartConversationClicked(getActivity().findViewById(item.getItemId()));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void onTicketCreated(SupportTicket ticket) {
        if (mAdapter != null) mAdapter.addTicket(ticket);
    }

    private void setStatusLoading() {
        mAdapterEmptyView.setVisibility(View.INVISIBLE);
        if (mAdapter != null && !mAdapter.isEmpty()) {
            mProgressView.setVisibility(View.INVISIBLE);
        } else {
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    private void setStatusReady() {
        if (mAdapter == null || mAdapter.isEmpty()) {
            mAdapterEmptyView.setVisibility(View.VISIBLE);
        } else {
            mAdapterEmptyView.setVisibility(View.INVISIBLE);
        }

        mProgressView.setVisibility(View.INVISIBLE);
    }

    private void setStatusFailure(String error) {
        mAdapterEmptyView.setVisibility(View.INVISIBLE);
        mProgressView.setVisibility(View.INVISIBLE);
    }


    public void refreshConversationList() {
        if (DBG) Log.v(TAG, "refreshConversationList");

        if (!mRefreshConversationsSubscription.isUnsubscribed()) return;

        setStatusLoading();

        Observable<SupportTickets> observable = AppObservable.bindFragment(this,
                mApiSupport.getTickets());

        mRefreshConversationsSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .subscribe(new Observer<SupportTickets>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable error) {
                        if (mListView == null) return;
                        setStatusFailure(getResources().getString(R.string.load_ticket_list_error));
                        if (mListener != null) mListener.notifyError(getResources().getString(R.string.load_ticket_list_error), error);
                    }

                    @Override
                    public void onNext(SupportTickets supportTickets) {
                        if (mListView == null) return;
                        if (mAdapter != null) mAdapter.setTickets(supportTickets.items);
                        setStatusReady();
                    }
                });
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
    public interface OnFragmentInteractionListener {
        void onStartConversationClicked(@Nullable View animateFrom);
        void onOpenConversationClicked(View view, SupportTicket ticket);
        void notifyError(String error, Throwable e);
    }

}
