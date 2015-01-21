package com.w1.merchant.android.support;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.UploadFileResponse;
import com.w1.merchant.android.service.ApiSupport;
import com.w1.merchant.android.utils.ContentTypedOutput;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;

import java.io.File;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreateTicketFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String SHARED_PREFS_NAME = "CreateTicketFragment";
    private static final String SHARED_PREFS_KEY_SUBJECT = "subject";
    private static final String SHARED_PREFS_KEY_TEXT = "text";
    private static final String SHARED_PREFS_KEY_PHOTO_URI = "photo_uri";

    private static final int REQUEST_PICK_PHOTO = Activity.RESULT_FIRST_USER + 100;

    private static final int REQUEST_TAKE_PHOTO = Activity.RESULT_FIRST_USER + 101;

    private EditText mSubjectView;
    private EditText mTextView;
    private View mProgress;

    private ImageView mAttachedImageView;

    private boolean mCreatingForm;

    private OnFragmentInteractionListener mListener;

    @Nullable
    private Uri mMakePhotoDstUri;

    public static CreateTicketFragment newInstance() {
        return new CreateTicketFragment();
    }

    public CreateTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_ticket, container, false);
        mSubjectView = (EditText)root.findViewById(R.id.subject);
        mTextView = (EditText)root.findViewById(R.id.text);
        mProgress = root.findViewById(R.id.progress);
        mAttachedImageView = (ImageView)root.findViewById(R.id.attached_image);

        mSubjectView.setHorizontallyScrolling(false);
        mSubjectView.setMaxLines(Integer.MAX_VALUE);

        AttachedImageClickListener clickListener = new AttachedImageClickListener();
        mAttachedImageView.setOnClickListener(clickListener);
        mAttachedImageView.setOnLongClickListener(clickListener);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DBG) Log.v(TAG, "onActivityResult()");

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_PHOTO:
                    mMakePhotoDstUri = data.getData();
                    assert mMakePhotoDstUri != null;
                    getActivity().getSharedPreferences(SHARED_PREFS_NAME, 0)
                            .edit()
                            .putString(SHARED_PREFS_KEY_PHOTO_URI, mMakePhotoDstUri.toString())
                            .commit(); // Чтобы нормально его восстановить сразу же в onResume
                    refreshAttachedImageView();
                    break;
                case REQUEST_TAKE_PHOTO:
                    refreshAttachedImageView();
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_create_ticket, menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveInputValues();
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreInputValues();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSubjectView = null;
        mTextView = null;
        mProgress = null;
        mAttachedImageView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_send:
                onSendButtonClicked();
                break;
            case R.id.action_attach:
                onAttachButtonClicked(getActivity().findViewById(R.id.action_attach));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void showAttachedImageActionView(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.attached_file_actions, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.remove_attachment:
                        removeAttachment();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popup.show();
    }

    private void removeAttachment() {
        if (mMakePhotoDstUri == null) return;
        if (Utils.isInPicturesDirectory(getActivity(), mMakePhotoDstUri)) {
            if (DBG) Log.v(TAG, "delete file " + mMakePhotoDstUri.getPath());
            new File(mMakePhotoDstUri.getPath()).delete();
        }
        mMakePhotoDstUri = null;
        refreshAttachedImageView();
    }

    private void refreshAttachedImageView() {
        if (mAttachedImageView == null) return;
        if (mMakePhotoDstUri == null) {
            mAttachedImageView.setVisibility(View.GONE);
            return;
        }
        mAttachedImageView.setVisibility(View.VISIBLE);
        Picasso.with(getActivity())
                .load(mMakePhotoDstUri)
                .placeholder(R.drawable.image_loading_drawable)
                .into(mAttachedImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        if (DBG) Log.v(TAG, "load image error");
                        mMakePhotoDstUri = null;
                        mAttachedImageView.setVisibility(View.GONE);
                    }
                });
    }

    private void onSendButtonClicked() {
        if (mCreatingForm) return;
        boolean formValid = validateForm();
        if (!formValid) return;
        createTicket();
    }

    private void onAttachButtonClicked(View view) {
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
                            removeAttachment();
                            takePictureIntent = Utils.createMakePhotoIntent(getActivity());
                            mMakePhotoDstUri = takePictureIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
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

    boolean validateForm() {
        if (TextUtilsW1.isBlank(mTextView.getText())) {
            Toast.makeText(getActivity(), R.string.text_should_not_be_empty, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setProgress(boolean inProgress) {
        if (mTextView == null) return;
        mProgress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        mTextView.setEnabled(!inProgress);
        mSubjectView.setEnabled(!inProgress);
    }

    void createTicket() {
        if (getActivity() == null) return;
        if (mCreatingForm) return;

        ApiSupport api = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);

        mCreatingForm = true;
        setProgress(true);

        if (mMakePhotoDstUri != null) {
            api.uploadFile(new ContentTypedOutput(getActivity(), mMakePhotoDstUri, null), new Callback<UploadFileResponse>() {
                @Override
                public void success(UploadFileResponse response, Response response2) {
                    sendMessage(response.getLinkAImg());
                    removeAttachment();
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mListener != null)
                        mListener.notifyError(getText(R.string.send_message_error), error);
                    setProgress(false);
                }
            });
        } else {
            sendMessage(null);
        }

    }

    private void sendMessage(@Nullable String imageAttachLink) {
        String msgText = TextUtilsW1.removeTrailingWhitespaces(mTextView.getText()).toString();
        if (imageAttachLink != null) msgText = msgText + "\n" + imageAttachLink;

        ApiSupport api = NetworkUtils.getInstance().createRestAdapter().create(ApiSupport.class);
        SupportTicket.CreateRequest req = new SupportTicket.CreateRequest(
                TextUtilsW1.removeTrailingWhitespaces(mSubjectView.getText()).toString(),
                msgText,
                getResources()
        );
        api.createTicket(req, new Callback<SupportTicket>() {
            @Override
            public void success(SupportTicket supportTicket, Response response) {
                if (mListener != null) mListener.onSupportTicketCreated(supportTicket);
                mSubjectView.setText("");
                mTextView.setText("");
                clearSharedPrefs();
                setProgress(false);
            }

            @Override
            public void failure(RetrofitError error) {
                setProgress(false);
                if (mListener != null) mListener.notifyError(getText(R.string.send_message_error), error);
            }
        });
    }

    private void clearSharedPrefs() {
        if (mSubjectView == null || getActivity() == null) return;
        getActivity().getSharedPreferences(SHARED_PREFS_NAME,0).edit().clear().commit();
    }

    private void saveInputValues() {
        if (getActivity() == null) return;

        CharSequence subject = mSubjectView.getText();
        CharSequence text = mTextView.getText();

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(SHARED_PREFS_NAME, 0)
                .edit();
        try {
            editor.clear();
            if (!TextUtilsW1.isBlank(subject)) editor.putString(SHARED_PREFS_KEY_SUBJECT, TextUtilsW1.removeTrailingWhitespaces(subject).toString());
            if (!TextUtilsW1.isBlank(text)) editor.putString(SHARED_PREFS_KEY_TEXT, TextUtilsW1.removeTrailingWhitespaces(text).toString());
            if (mMakePhotoDstUri != null) editor.putString(SHARED_PREFS_KEY_PHOTO_URI, mMakePhotoDstUri.toString());
        } finally {
            editor.apply();
        }
    }

    private void restoreInputValues() {
        if (mSubjectView == null || getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFS_NAME, 0);
        String subject = prefs.getString(SHARED_PREFS_KEY_SUBJECT, "");
        String text = prefs.getString(SHARED_PREFS_KEY_TEXT, "");
        String photoUri = prefs.getString(SHARED_PREFS_KEY_PHOTO_URI, null);
        mMakePhotoDstUri = photoUri == null ? null : Uri.parse(photoUri);
        mSubjectView.setText(subject);
        mTextView.setText(text);
        refreshAttachedImageView();
    }

    private class AttachedImageClickListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            showAttachedImageActionView(v);
        }

        @Override
        public boolean onLongClick(View v) {
            showAttachedImageActionView(v);
            return true;
        }
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
        public void onSupportTicketCreated(SupportTicket ticket);
        public void notifyError(CharSequence text, @Nullable Throwable error);
    }

}
