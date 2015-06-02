package com.w1.merchant.android.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.Profile;
import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.model.UploadFileResponse;
import com.w1.merchant.android.service.ApiProfile;
import com.w1.merchant.android.service.ApiSupport;
import com.w1.merchant.android.utils.ContentTypedOutput;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class ConversationFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String ARG_TICKET = "com.w1.merchant.android.support.ARG_TICKET";

    private static final String BUNDLE_KEY_TICKET = "com.w1.merchant.android.support.ConversationFragment.BUNDLE_KEY_TICKET";
    private static final String BUNDLE_KEY_POSTS = "com.w1.merchant.android.support.ConversationFragment.BUNDLE_KEY_POSTS";
    private static final String BUNDLE_KEY_PHOTO_DST_URI = "com.w1.merchant.android.support.ConversationFragment.BUNDLE_KEY_PHOTO_DST_URI";

    private static final int REQUEST_PICK_PHOTO = Activity.RESULT_FIRST_USER + 100;

    private static final int REQUEST_TAKE_PHOTO = Activity.RESULT_FIRST_USER + 101;

    private OnFragmentInteractionListener mListener;

    @Nullable
    private SupportTicket mTicket;

    private RecyclerView mListView;

    private ConversationAdapter mAdapter;

    private EditText mSendMessageText;
    private View mSendMessageButton;
    private View mSendMessageProgress;
    private View mAttachButton;

    @Nullable
    private Uri mMakePhotoDstUri;

    private Subscription mPostMessageSubscription = Subscriptions.unsubscribed();
    private Subscription mRefreshMessagesSubscription = Subscriptions.unsubscribed();
    private Subscription mProfileSubscription = Subscriptions.unsubscribed();

    public static ConversationFragment newInstance(@Nullable SupportTicket ticket) {
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

        if (savedInstanceState != null) {
            mTicket = savedInstanceState.getParcelable(ARG_TICKET);
            mMakePhotoDstUri = savedInstanceState.getParcelable(BUNDLE_KEY_PHOTO_DST_URI);
        } else {
            mTicket = getArguments().getParcelable(ARG_TICKET);
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

        mAdapter = new ConversationAdapter(getActivity()) {
            @Override
            public ViewHolderMessage onCreateViewHolder(ViewGroup parent, int viewType) {
                final ViewHolderMessage holder = super.onCreateViewHolder(parent, viewType);
                if (holder instanceof ViewHolderTheirMessage) {
                    // Клик по аватару
                    ((ViewHolderTheirMessage) holder).avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListView == null) return;
                            SupportTicketPost post = mAdapter.getMessage(holder);
                            if (post != null) {
                                SupportProfileActivity.startActivity(v.getContext(), post, v);
                            }
                        }
                    });
                }
                return holder;
            }
        };

        if (savedInstanceState != null) {
            List<SupportTicketPost> tickets = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_POSTS);
            if (tickets != null) mAdapter.setMessages(tickets);
        } else {
            if (mTicket == null) {
                mAdapter.setMessages(Collections.singletonList(SupportTicketPost.createMayIHelpYouFakePost(getResources())));
            } else {
                mAdapter.setMessages(mTicket.posts);
            }
        }

        mListView.setAdapter(mAdapter);
        initSendMessageForm();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        smoothScrollToEnd();

        if (mTicket == null && savedInstanceState == null) {
            mSendMessageText.requestFocus();
            mSendMessageText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSendMessageText == null) return;
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(mSendMessageText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 50);
        }
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
        if (mTicket != null) outState.putParcelable(BUNDLE_KEY_TICKET, mTicket);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPostMessageSubscription.unsubscribe();
        mRefreshMessagesSubscription.unsubscribe();
        mProfileSubscription.unsubscribe();
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

    /**
     * Отправка сообщения. Создание диалога, если он ещё не создан
     * @param message
     */
    private void sendMessage(String message) {
        if (mTicket == null) {
            // Перед созданием диалога, загружаем профиль для отправки данных юзера
            loadProfile(message);
        } else {
            sendMessageHasDialog(message);
        }
    }

    /**
     * Отправка сообщения в существующий диалог
     * @param message
     */
    private void sendMessageHasDialog(String message) {
        ApiSupport apiMessenger = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        mPostMessageSubscription.unsubscribe();

        setupStatusSending();
        Assert.assertNotNull(mTicket);
        Observable<SupportTicketPost> observable = AppObservable.bindFragment(this,
                apiMessenger.postReply(mTicket.ticketId, new SupportTicket.ReplyRequest(message)));
        mPostMessageSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupStatusReady();
                    }
                })
                .subscribe(new Observer<SupportTicketPost>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null)
                            mListener.notifyError(getText(R.string.load_ticket_error), e);
                    }

                    @Override
                    public void onNext(SupportTicketPost supportTicketPost) {
                        if (mListView == null) return;
                        addMessageScrollToEnd(supportTicketPost);
                        mSendMessageText.setText("");
                    }
                });
    }

    void loadProfile(final String originalMessage) {
        mProfileSubscription.unsubscribe();

        ApiProfile apiProfile = NetworkUtils.getInstance().createRestAdapter().create(ApiProfile.class);
        Observable<Profile> observable = AppObservable.bindFragment(this, apiProfile.getProfile());

        mProfileSubscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .subscribe(new Observer<Profile>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        createDialog(originalMessage, null); // Хрен с ними с данными
                    }

                    @Override
                    public void onNext(Profile profile) {
                        createDialog(originalMessage, profile);
                    }
                });
    }

    private void createDialog(String message, @Nullable Profile profile) {
        ApiSupport apiMessenger = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        mPostMessageSubscription.unsubscribe();

        setupStatusSending();

        if (profile != null) message = formatMerchantInfo(profile) + "\n\n" + message;
        SupportTicket.CreateRequest req = new SupportTicket.CreateRequest(
                getResources().getString(R.string.new_conversation_subject), message, getResources());
        Observable<SupportTicket> observable = AppObservable.bindFragment(this,
                apiMessenger.createTicket(req));
        mPostMessageSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupStatusReady();
                    }
                })
                .subscribe(new Observer<SupportTicket>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null)
                            mListener.notifyError(getText(R.string.send_message_error), e);
                    }

                    @Override
                    public void onNext(SupportTicket supportTicket) {
                        if (mListView == null) return;
                        if (mListener != null) mListener.onSupportTicketCreated(supportTicket);
                        mTicket = supportTicket;
                        mSendMessageText.setText("");
                        mAdapter.setMessages(supportTicket.posts);}
                });

    }

    private void sendImage(Uri imageUri) {
        if (DBG) Log.v(TAG, "sendImage image uri: " + imageUri);
        ApiSupport apiMessenger = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        mPostMessageSubscription.unsubscribe();

        setupStatusSending();

        Observable<UploadFileResponse> observable = AppObservable.bindFragment(this,
                apiMessenger.uploadFile(new ContentTypedOutput(getActivity(), imageUri, null)));

        mPostMessageSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        deleteTakenPicture();
                    }
                })
                .subscribe(new Observer<UploadFileResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null)
                            mListener.notifyError(getText(R.string.send_message_error), e);
                        setupStatusReady();
                    }

                    @Override
                    public void onNext(UploadFileResponse response) {
                        String message = mSendMessageText.getText().toString() + "\n" + response.getLinkAImg();
                        sendMessage(message);
                    }
                });
    }

    String formatMerchantInfo(Profile profile) {
        Resources resources = getResources();
        List<String> info = new ArrayList<>(5);

        info.add(resources.getString(R.string.profile_info_wallet_id, TextUtilsW1.formatUserId(profile.userId)));

        String name = profile.getName();
        if (!TextUtils.isEmpty(name)) {
            info.add(resources.getString(R.string.profile_info_name, name));
        }

        Profile.Attribute phone = profile.findAttribute(Profile.Attribute.ATTRIBUTE_TYPE_PHONE_NUMBER);
        if (phone != null) {
            info.add(resources.getString(R.string.profile_info_phone, phone.displayValue));
        }

        Profile.Attribute email = profile.findAttribute(Profile.Attribute.ATTRIBUTE_TYPE_EMAIL);
        if (email != null) {
            info.add(resources.getString(R.string.profile_info_email, email.displayValue));
        }

        Profile.Attribute url = profile.findMerchantUrl();
        if (url != null) {
            info.add(resources.getString(R.string.profile_info_merchant_url, url.displayValue));
        }

        if (DBG) Log.v(TAG, "profile info: " + TextUtils.join("; ", info));

        return TextUtils.join("\n", info);
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
        if (!mRefreshMessagesSubscription.isUnsubscribed()) return;
        if (mTicket == null) return;

        ApiSupport api = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);
        if (showSpinner) getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);

        Observable<SupportTicket> observable = AppObservable.bindFragment(this,
                api.getTicket(mTicket.ticketId));

        mRefreshMessagesSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        if (getView() != null) getView().findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                    }
                })
                .subscribe(new Observer<SupportTicket>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null)
                            mListener.notifyError(getText(R.string.load_ticket_error), e);
                    }

                    @Override
                    public void onNext(SupportTicket supportTicket) {
                        if (mAdapter == null) return;
                        addMessagesDoNotScrollList(supportTicket.posts);
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
        void notifyError(CharSequence text, @Nullable Throwable error);
        void onSupportTicketCreated(SupportTicket ticket);
    }

}
