package com.w1.merchant.android.support;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.model.UploadFileResponse;
import com.w1.merchant.android.service.ApiSupport;
import com.w1.merchant.android.utils.ContentTypedOutput;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConversationFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String ARG_TICKET = "com.w1.merchant.android.support.ARG_TICKET";

    private static final String BUNDLE_KEY_POSTS = "com.w1.merchant.android.support.BUNDLE_KEY_POSTS";
    private static final String BUNDLE_KEY_PHOTO_DST_URI = "com.w1.merchant.android.support.BUNDLE_KEY_PHOTO_DST_URI";

    private static final int REFRESH_PERIOD = 60;

    private static final int REQUEST_PICK_PHOTO = Activity.RESULT_FIRST_USER + 100;

    private static final int REQUEST_TAKE_PHOTO = Activity.RESULT_FIRST_USER + 101;

    private OnFragmentInteractionListener mListener;

    private SupportTicket mTicket;

    private RecyclerView mListView;

    private ConversationAdapter mAdapter;

    private EditText mSendMessageText;
    private View mSendMessageButton;
    private View mSendMessageProgress;
    private View mAttachButton;

    private boolean mLoading;

    private Handler mRefreshHandler;

    @Nullable
    private Uri mMakePhotoDstUri;

    public static ConversationFragment newInstance(SupportTicket ticket) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TICKET, ticket);
        fragment.setArguments(args);
        return fragment;
    }

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTicket = getArguments().getParcelable(ARG_TICKET);
        if (savedInstanceState != null) {
            mMakePhotoDstUri = savedInstanceState.getParcelable(BUNDLE_KEY_PHOTO_DST_URI);
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;
        if (DBG) Log.v(TAG, "onActivityResult()");

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_PHOTO:
                    imageUri = data.getData();
                    sendImage(imageUri);
                    break;
                case REQUEST_TAKE_PHOTO:
                    imageUri = mMakePhotoDstUri;
                    sendImage(imageUri);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = getActivity().getLayoutInflater().inflate(R.layout.fragment_conversation, container, false);

        mSendMessageText = (EditText)root.findViewById(R.id.reply_to_comment_text);
        mSendMessageButton = root.findViewById(R.id.reply_to_comment_button);
        mSendMessageProgress = root.findViewById(R.id.reply_to_comment_progress);
        mAttachButton = root.findViewById(R.id.attach_button);

        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachButtonClicked(v);
            }
        });

        mListView = (RecyclerView) root.findViewById(R.id.recycler_list_view);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setStackFromEnd(true);
        mListView.setLayoutManager(lm);
        mListView.getItemAnimator().setAddDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        mListView.getItemAnimator().setSupportsChangeAnimations(true);

        mAdapter = new ConversationAdapter(getActivity());
        if (savedInstanceState != null) {
            List<SupportTicketPost> tickets = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_POSTS);
            if (tickets != null) mAdapter.setMessages(tickets);
        } else {
            mAdapter.setMessages(mTicket.posts);
        }

        mListView.setAdapter(mAdapter);
        initSendMessageForm();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRefreshHandler = new Handler();
        smoothScrollToEnd();
    }

    @Override
    public void onStart() {
        super.onStart();
        startPeriodicRefresh();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPeriodicRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMessages(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<SupportTicketPost> posts = mAdapter.getMessages();
        if (!posts.isEmpty()) outState.putParcelableArrayList(BUNDLE_KEY_POSTS, new ArrayList<>(posts));
        if (mMakePhotoDstUri != null) outState.putParcelable(BUNDLE_KEY_PHOTO_DST_URI, mMakePhotoDstUri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mPostMessageSubscription.unsubscribe();
        mSendMessageText = null;
        mSendMessageButton = null;
        mSendMessageProgress = null;
        mAttachButton = null;
        mListView.setOnScrollListener(null);
        mListView = null;
        mAdapter = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRefreshHandler = null;
    }

    public void onImeKeyboardShown() {
        if (DBG) Log.v(TAG, "onImeKeyboardShown");
        smoothScrollToEnd();
    }

    void onAttachButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.attach_file_actions, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.take_photo:
                        Intent takePictureIntent;
                        try {
                            takePictureIntent = Utils.createMakePhotoIntent(getActivity());
                            mMakePhotoDstUri = takePictureIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                            new File(mMakePhotoDstUri.getPath()).deleteOnExit();
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        } catch (Utils.MakePhotoException e) {
                            Toast.makeText(getActivity(), e.errorResourceId, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.pick_from_gallery:
                        Intent photoPickerIntent = Utils.createPickPhotoActivityIntent();
                        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popup.show();
    }

    private void initSendMessageForm() {
        mSendMessageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.send_reply_to_comment) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        mSendMessageButton.setEnabled(mSendMessageText.length() != 0);
        mSendMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mSendMessageButton.setEnabled(s.length() != 0);
            }
        });
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String comment = mSendMessageText.getText().toString();
        if (DBG && comment.isEmpty()) throw new IllegalStateException("Button must be disabled");
        sendMessage(comment);
    }

    private void sendMessage(String message) {
        ApiSupport apiMessenger = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        setupStatusSending();

        apiMessenger.postReply(mTicket.ticketId, new SupportTicket.ReplyRequest(message), new Callback<SupportTicketPost>() {
            @Override
            public void success(SupportTicketPost supportTicket, Response response) {
                if (mListView == null) return;
                addMessageScrollToEnd(supportTicket);
                mSendMessageText.setText("");
                setupStatusReady();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mListener != null)
                    mListener.notifyError(getText(R.string.load_ticket_error), error);
                setupStatusReady();
            }
        });
    }

    private void sendImage(Uri imageUri) {
        if (DBG) Log.v(TAG, "sendImage image uri: " + imageUri);
        ApiSupport apiMessenger = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        setupStatusSending();
        apiMessenger.uploadFile(new ContentTypedOutput(getActivity(), imageUri, null), new Callback<UploadFileResponse>() {
            @Override
            public void success(UploadFileResponse response, Response response2) {
                sendMessage(mSendMessageText.getText().toString() + "\n" + response.getLinkAImg());
                deleteTakenPicture();
            }

            @Override
            public void failure(RetrofitError error) {
                if (mListener != null)
                    mListener.notifyError(getText(R.string.send_message_error), error);
                setupStatusReady();
                deleteTakenPicture();
            }
        });
    }

    private void setupStatusSending() {
        if (mSendMessageText == null) return;
        mAttachButton.setEnabled(false);
        mSendMessageButton.setEnabled(false);
        mSendMessageText.setEnabled(false);
        mSendMessageProgress.setVisibility(View.VISIBLE);
        mSendMessageButton.setVisibility(View.INVISIBLE);
    }

    private void setupStatusReady() {
        if (mSendMessageText == null) return;
        mAttachButton.setEnabled(true);
        mSendMessageButton.setEnabled(true);
        mSendMessageText.setEnabled(true);
        mSendMessageProgress.setVisibility(View.INVISIBLE);
        mSendMessageButton.setVisibility(View.VISIBLE);
    }

    private void deleteTakenPicture() {
        if (mMakePhotoDstUri != null) {
            new File(mMakePhotoDstUri.getPath()).delete();
            mMakePhotoDstUri = null;
        }
    }

    private void refreshMessages(boolean showSpinner) {
        if (mLoading) return;
        ApiSupport api = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);
        if (showSpinner) getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        api.getTicket(mTicket.ticketId, new Callback<SupportTicket>() {
            @Override
            public void success(SupportTicket supportTicket, Response response) {
                mLoading = false;
                if (mAdapter == null) return;
                addMessagesDoNotScrollList(supportTicket.posts);
                getView().findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }

            @Override
            public void failure(RetrofitError error) {
                mLoading = false;
                if (getView() == null) return;
                getView().findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                if (mListener != null)
                    mListener.notifyError(getText(R.string.load_ticket_error), error);
            }
        });
    }

    private void addMessageScrollToEnd(SupportTicketPost message) {
        if (mAdapter == null) return;

        mAdapter.addMessage(message);
        smoothScrollToEnd();
    }

    private void smoothScrollToEnd() {
        if (mListView == null) return;
        if (mAdapter == null) return;
        final int newPosition = mAdapter.getLastPosition() + 1;
        if (DBG) Log.v(TAG, "scrollListToPosition pos: " + newPosition);
        mListView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (mListView == null) return true;
                if (mListView.getViewTreeObserver().isAlive()) {
                    mListView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mListView.smoothScrollToPosition(newPosition);
                    return false;
                }
                return true;
            }
        });
    }

    private void addMessagesDoNotScrollList(List<SupportTicketPost> messages) {
        Long oldTopId = null;
        int oldTopTop = 0;

        ConversationAdapter.ViewHolderMessage top = findTopVisibleMessageViewHolder();
        if (top != null) {
            oldTopId = mAdapter.getItemId(top.getPosition());
            oldTopTop = top.itemView.getTop();
        }

        mAdapter.addMessages(messages);

        if (oldTopId != null) {
            Integer newPosition = mAdapter.findPositionById(oldTopId);
            if (newPosition != null) {
                LinearLayoutManager lm = (LinearLayoutManager) mListView.getLayoutManager();
                lm.scrollToPositionWithOffset(newPosition, oldTopTop);
            }
        }
    }

    private void startPeriodicRefresh() {
        if (mRefreshHandler == null) return;
        mRefreshHandler.postDelayed(mPeriodicRefreshRunnable, REFRESH_PERIOD * 1000);
    }

    private void stopPeriodicRefresh() {
        if (mRefreshHandler == null) return;
        mRefreshHandler.removeCallbacks(mPeriodicRefreshRunnable);
    }

    private final Runnable mPeriodicRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (mListView == null || mRefreshHandler == null) return;
            if (DBG) Log.v(TAG, "periodic refresh");
            refreshMessages(false);
            mRefreshHandler.postDelayed(this, REFRESH_PERIOD * 1000);
        }
    };

    @Nullable
    private ConversationAdapter.ViewHolderMessage findTopVisibleMessageViewHolder() {
        if (mListView == null) return null;
        int count = mListView.getChildCount();
        for (int i = 0; i < count; ++i) {
            RecyclerView.ViewHolder holder = mListView.getChildViewHolder(mListView.getChildAt(i));
            if (holder instanceof ConversationAdapter.ViewHolderMessage) return (ConversationAdapter.ViewHolderMessage) holder;
        }
        return null;
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
        public void notifyError(CharSequence text, @Nullable Throwable error);
    }

}
