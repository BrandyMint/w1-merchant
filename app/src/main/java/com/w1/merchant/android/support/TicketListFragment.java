package com.w1.merchant.android.support;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
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
import com.w1.merchant.android.viewextended.DividerItemDecoration;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    private boolean mLoading;

    private Handler mRefreshDatesHandler;

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

        mRefreshDatesHandler = new Handler();

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
    public void onStart() {
        super.onStart();
        startRefreshRelativeDates();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshConversationList();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRefreshRelativeDates();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mAdapter.getTickets().isEmpty()) outState.putParcelableArrayList(BUNDLE_KEY_SUPPORT_TICKETS, mAdapter.getTickets());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRefreshRelativeDates();
        mRefreshDatesHandler = null;
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
                if (mListener != null) mListener.onStartConversationClicked();
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

        if (mLoading) {
            return;
        } else {
            mLoading = true;
        }

        setStatusLoading();
        mApiSupport.getTickets(new Callback<SupportTickets>() {
            @Override
            public void success(SupportTickets supportTickets, Response response) {
                mLoading = false;
                if (mListView == null) return;
                if (mAdapter != null) mAdapter.setTickets(supportTickets.items);
                setStatusReady();
            }

            @Override
            public void failure(RetrofitError error) {
                mLoading = false;
                if (mListView == null) return;
                setStatusFailure(getResources().getString(R.string.load_ticket_list_error));
                if (mListener != null) mListener.notifyError(getResources().getString(R.string.load_ticket_list_error), error);
            }
        });
    }


    private void startRefreshRelativeDates() {
        mRefreshDatesHandler.removeCallbacks(mRefreshDatesRunnable);
        mRefreshDatesHandler.postDelayed(mRefreshDatesRunnable, REFRESH_DATES_PERIOD * 1000);
    }

    private void stopRefreshRelativeDates() {
        mRefreshDatesHandler.removeCallbacks(mRefreshDatesRunnable);
    }

    private final Runnable mRefreshDatesRunnable = new Runnable() {
        @Override
        public void run() {
            if (mListView == null || mAdapter == null || mRefreshDatesHandler == null) return;
            if (DBG) Log.v(TAG, "refreshRelativeDates");
            mAdapter.refreshRelativeDates(mListView);
            mRefreshDatesHandler.postDelayed(mRefreshDatesRunnable, REFRESH_DATES_PERIOD * 1000);
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
        public void onStartConversationClicked();
        public void onOpenConversationClicked(View view, SupportTicket ticket);
        public void notifyError(String error, Throwable e);
    }

}
