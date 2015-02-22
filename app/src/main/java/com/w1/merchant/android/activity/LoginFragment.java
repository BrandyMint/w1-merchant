package com.w1.merchant.android.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.model.AuthCreateModel;
import com.w1.merchant.android.model.AuthModel;
import com.w1.merchant.android.model.AuthPrincipalRequest;
import com.w1.merchant.android.model.OneTimePassword;
import com.w1.merchant.android.model.PrincipalUser;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiSessions;
import com.w1.merchant.android.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.client.Response;

public class LoginFragment extends Fragment {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String APP_PREF = "logins";
    private static final long VIBRATE_TIME_MS = 100;
    private static final String PREFS_KEY_LOGINS = "logins";

    private AutoCompleteTextView mLoginTextView;
    private View mAuthButton;
    private View mForgotButton;
    private View mClearLoginButton;
    private EditText mPasswordView;
    private Vibrator mVibrator;
    private ProgressBar mProgress;

    private final Matcher mLooksLikePhoneMatcher = Pattern.compile("[0-9]{11}").matcher("");

    private ArrayList<String> mLogins = new ArrayList<>();

    private ApiSessions mApiSessions;

    private String mLogin;

    private OnFragmentInteractionListener mListener;

    public LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {

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
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mApiSessions = NetworkUtils.getInstance().createRestAdapter().create(ApiSessions.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root  = inflater.inflate(R.layout.fragment_login, container, false);

        mLoginTextView = (AutoCompleteTextView) root.findViewById(R.id.actvLogin);
        mProgress = (ProgressBar) root.findViewById(R.id.pbLogin);
        mAuthButton = root.findViewById(R.id.tvAuth);
        mClearLoginButton = root.findViewById(R.id.ivDelete);
        mPasswordView = (EditText) root.findViewById(R.id.etPassword);
        mForgotButton = root.findViewById(R.id.tvForgot);

        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tvAuth:
                        attemptLogin();
                        break;
                    case R.id.ivDelete:
                        mLoginTextView.setText("");
                        mLoginTextView.requestFocus();
                        break;
                    case R.id.tvForgot:
                        sendOneTimePassword();
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        };

        mAuthButton.setOnClickListener(onClickListener);
        mClearLoginButton.setOnClickListener(onClickListener);
        mForgotButton.setOnClickListener(onClickListener);

        setProgress(false);
        adjustBackgroundImageSizes(root);
        initScroller(root);
        loadLogins();

        mLoginTextView.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, mLogins));
        mLoginTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (mClearLoginButton == null) return;
                mClearLoginButton.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                mAuthButton.setEnabled(isFormReadyToSend());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });
        mLoginTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPasswordView != null) {
                    mPasswordView.requestFocus();
                }
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mForgotButton != null) {
                    mForgotButton.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (BuildConfig.DEBUG && "debug".equals(BuildConfig.BUILD_TYPE) && !TextUtils.isEmpty(BuildConfig.API_TEST_USER)) {
            // Ибо заебал
            attemptLogin(BuildConfig.API_TEST_USER, BuildConfig.API_TEST_PASS);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoginTextView = null;
        mAuthButton = null;
        mForgotButton = null;
        mClearLoginButton = null;
        mPasswordView = null;
        mProgress = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVibrator = null;
        mApiSessions = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void adjustBackgroundImageSizes(final View root) {
        final ImageView bottomView = (ImageView) root.findViewById(R.id.background_bottom_image);
        bottomView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (bottomView.getViewTreeObserver().isAlive()) {
                    int rootHeight = root.findViewById(R.id.root).getRootView().getHeight();
                    if (rootHeight == 0) {
                        if (DBG) Log.v(TAG, "onPreDrawListener root height is 0");
                        return true;
                    }

                    bottomView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int authButtonLoc[] = new int[]{0, 0};
                    mAuthButton.getLocationOnScreen(authButtonLoc);

                    int below = rootHeight - authButtonLoc[1];
                    bottomView.setMaxHeight(below);
                    //ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) bottomView.getLayoutParams();
                    //lp.topMargin = authButtonLoc[1];
                    //bottomView.setLayoutParams(lp);
                    int top0 = root.findViewById(R.id.root).getHeight() - bottomView.getHeight();
                    bottomView.setTranslationY(Math.max(top0, authButtonLoc[1]));
                    bottomView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }

    private void initScroller(final View root) {
        final View activityRootView = root.findViewById(R.id.root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean imeKeyboardShown;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!imeKeyboardShown) {
                        imeKeyboardShown = true;
                        final ScrollView scrollView = (ScrollView) root.findViewById(R.id.scroll_view);
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight());
                            }
                        }, 64);
                    }
                } else {
                    if (imeKeyboardShown) {
                        imeKeyboardShown = false;
                    }
                }
            }
        });
    }

    private void setProgress(boolean inProgress) {
        mProgress.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
        mLoginTextView.setEnabled(!inProgress);
        mPasswordView.setEnabled(!inProgress);
        mForgotButton.setEnabled(!inProgress);
        mClearLoginButton.setEnabled(!inProgress);
        mAuthButton.setEnabled(!inProgress && isFormReadyToSend());
    }

    private void attemptLogin() {
        if (!validateForm()) return;
        mVibrator.vibrate(VIBRATE_TIME_MS);
        attemptLogin(mLoginTextView.getText().toString(), mPasswordView.getText().toString());
    }

    private abstract class ApiSubrequestTask<T> extends ApiRequestTask<T> {

        @Nullable
        @Override
        protected Activity getContainerActivity() {
            return LoginFragment.this.getActivity();
        }

        @Override
        protected void onFailure(NetworkUtils.ResponseErrorException error) {
            if (DBG) Log.v(TAG, "auth error", error);
            if (getContainerActivity() == null) return;
            Toast toast;
            CharSequence errMsg;

            errMsg = error.getErrorDescription();
            if (TextUtils.isEmpty(errMsg)) {
                switch (error.getHttpStatus()) {
                    case 400:
                        errMsg = getText(R.string.bad_password);
                        break;
                    case 404:
                        errMsg = getText(R.string.user_not_found);
                        break;
                    default:
                        errMsg = getText(R.string.network_error);
                        break;
                }
            }

            toast = Toast.makeText(getContainerActivity(), errMsg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
            Session.getInstance().clear();
            setProgress(false);
        }

        @Override
        protected void onCancelled() {
            if (getContainerActivity() == null) return;
            if (DBG) Toast.makeText(getContainerActivity(), R.string.canceled, Toast.LENGTH_SHORT).show();
            setProgress(false);
        }
    }

    private void attemptLogin(final String login, final String password) {

        mLogin = login;

        new ApiSubrequestTask<AuthModel>() {

            @Override
            protected void doRequest(Callback<AuthModel> callback) {
                mApiSessions.auth(new AuthCreateModel(login, password), callback);
            }

            @Override
            protected void onSuccess(AuthModel response, Response responseRaw) {
                if (DBG) Log.v(TAG, "auth success ");
                Session.getInstance().setAuth(response);
                requestPrincipalUsers(response);
            }
        }.execute();
        setProgress(true);
    }

    void requestPrincipalUsers(final AuthModel authModel) {
        new ApiSubrequestTask<List<PrincipalUser>>() {

            @Override
            protected void doRequest(Callback<List<PrincipalUser>> callback) {
                mApiSessions.getPrincipalUsers(callback);
            }

            @Override
            protected void onSuccess(List<PrincipalUser> principalUsers, Response response) {
                if (principalUsers == null || principalUsers.isEmpty()) {
                    onAuthDone(authModel);
                } else {
                    selectPrincipal(principalUsers.get(0));
                }
            }
        }.execute();
    }

    void selectPrincipal(final PrincipalUser user) {
        new ApiSubrequestTask<AuthModel>() {

            @Override
            protected void doRequest(Callback<AuthModel> callback) {
                mApiSessions.authPrincipal(new AuthPrincipalRequest(user.principalUserId), callback);
            }

            @Override
            protected void onSuccess(AuthModel authModel, Response response) {
                onAuthDone(authModel);
            }
        }.execute();
    }

    private void onAuthDone(AuthModel authModel) {
        Session.getInstance().setAuth(authModel);

        if (!TextUtils.isEmpty(mLogin) && !mLogins.contains(mLogin)) {
            mLogins.add(mLogin);
            persistLogins();
        }

        mPasswordView.setText("");
        mLoginTextView.setText("");
        mForgotButton.setVisibility(View.INVISIBLE);
        setProgress(false);
        if (mListener != null) mListener.onAuthDone();
    }

    private void sendOneTimePassword() {
        if (TextUtils.isEmpty(mLoginTextView.getText())) return;
        final String login = mLoginTextView.getText().toString();
        mVibrator.vibrate(VIBRATE_TIME_MS);

        setProgress(true);
        new ApiRequestTask<Void>() {

            @Override
            protected void doRequest(Callback<Void> callback) {
                mApiSessions.sendOneTimePassword(new OneTimePassword.Request(login), callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return LoginFragment.this.getActivity();
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (DBG) Log.v(TAG, "sendOneTimePassword error", error);
                setProgress(false);
                if (error.getHttpStatus() >= 200 && error.getHttpStatus() < 300) {
                    // Ignore malformed JSON ("")
                    onSuccess(null, error.getRetrofitError().getResponse());
                } else {
                    if (LoginFragment.this.getActivity() == null) return;
                    CharSequence errText = error.getErrorDescription(getText(R.string.failed_to_send_one_time_password));
                    Toast toast = Toast.makeText(LoginFragment.this.getActivity(), errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                    setProgress(false);
                }
            }

            @Override
            protected void onCancelled() {
                if (LoginFragment.this.getActivity() == null) return;
                if (DBG) Toast.makeText(LoginFragment.this.getActivity(), R.string.canceled, Toast.LENGTH_SHORT).show();
                setProgress(false);
            }

            @Override
            protected void onSuccess(Void aVoid, Response response) {
                if (LoginFragment.this.getActivity() == null) return;
                Toast toast = Toast.makeText(LoginFragment.this.getActivity(), getString(R.string.pass_sent,
                        mLoginTextView.getText().toString()), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
                setProgress(false);
            }
        }.execute();
    }

    //Проверка логина и пароля на пустоту
    public boolean validateForm() {
        boolean result;

        if (TextUtils.isEmpty(mLoginTextView.getText())) {
            mLoginTextView.setError(getText(R.string.error_field));
            result = false;
        } else result = true;
        if (TextUtils.isEmpty(mPasswordView.getText())) {
            if (result) mPasswordView.setError(getText(R.string.error_field));
            result = false;
        } else result = true;

        return result;
    }

    private boolean isFormReadyToSend() {
        String s = mLoginTextView.getText().toString();
        return s.contains("@") || mLooksLikePhoneMatcher.reset(s).matches();
    }

    private void persistLogins() {
        Set<String> newLogin = new HashSet<>();
        for (String n : mLogins) newLogin.add(n);
        getActivity().getSharedPreferences(APP_PREF, 0)
                .edit()
                .putStringSet(PREFS_KEY_LOGINS, newLogin)
                .apply();
    }

    private void loadLogins() {
        SharedPreferences preferences = getActivity().getSharedPreferences(APP_PREF, 0);
        Set<String> logins = preferences.getStringSet(PREFS_KEY_LOGINS, Collections.<String>emptySet());
        mLogins.addAll(logins);
    }

    public interface OnFragmentInteractionListener {
        public void onAuthDone();
    }

}
