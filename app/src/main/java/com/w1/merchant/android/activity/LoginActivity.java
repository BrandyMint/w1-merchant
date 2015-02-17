package com.w1.merchant.android.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
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
import com.w1.merchant.android.extra.DialogNoInet;
import com.w1.merchant.android.extra.DialogTimeout;
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

public class LoginActivity extends Activity {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String APP_PREF = "W1_Pref";
    private static final long VIBRATE_TIME_MS = 100;
    private static final int ACT_MENU = 1;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if (!isNetworkConnected()) {
            DialogFragment dlgNoInet = new DialogNoInet();
            dlgNoInet.show(getFragmentManager(), "dlgNoInet");
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mApiSessions = NetworkUtils.getInstance().createRestAdapter().create(ApiSessions.class);

        mLoginTextView = (AutoCompleteTextView) findViewById(R.id.actvLogin);
        mProgress = (ProgressBar) findViewById(R.id.pbLogin);
        mAuthButton = findViewById(R.id.tvAuth);
        mClearLoginButton = findViewById(R.id.ivDelete);
        mPasswordView = (EditText) findViewById(R.id.etPassword);
        mForgotButton = findViewById(R.id.tvForgot);

        mAuthButton.setOnClickListener(mOnClickListener);
        mClearLoginButton.setOnClickListener(mOnClickListener);
        mForgotButton.setOnClickListener(mOnClickListener);
        setProgress(false);
        adjustBackgroundImageSizes();
        initScroller();
        loadLogins();

        mLoginTextView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, mLogins));
        mLoginTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
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
                mForgotButton.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });


        if (BuildConfig.DEBUG && "debug".equals(BuildConfig.BUILD_TYPE) && !TextUtils.isEmpty(BuildConfig.API_TEST_USER)) {
            // Ибо заебал
            //attemptLogin(BuildConfig.API_TEST_USER, BuildConfig.API_TEST_PASS);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACT_MENU) {
            if (resultCode == RESULT_OK) {
                DialogFragment dlgSessionTimeout = new DialogTimeout();
                dlgSessionTimeout.show(getFragmentManager(), "dlgSessionTimeout");
            }
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tvAuth:
                    attemptLogin();
                    break;
                case R.id.ivDelete:
                    mLoginTextView.setText("");
                    break;
                case R.id.tvForgot:
                    sendOneTimePassword();
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    };

    private void adjustBackgroundImageSizes() {
        final ImageView bottomView = (ImageView) findViewById(R.id.background_bottom_image);
        bottomView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (bottomView.getViewTreeObserver().isAlive()) {
                    bottomView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int authButtonLoc[] = new int[]{0, 0};
                    mAuthButton.getLocationOnScreen(authButtonLoc);
                    int rootHeight = findViewById(R.id.root).getRootView().getHeight();
                    int below = rootHeight - authButtonLoc[1];
                    bottomView.setMaxHeight(below);
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) bottomView.getLayoutParams();
                    lp.topMargin = authButtonLoc[1];
                    bottomView.setLayoutParams(lp);
                }
                return false;
            }
        });
    }

    private void initScroller() {
        final View activityRootView = findViewById(R.id.root);
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
                        final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
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
            return LoginActivity.this;
        }

        @Override
        protected void onFailure(NetworkUtils.ResponseErrorException error) {
            if (DBG) Log.v(TAG, "auth error", error);
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

            toast = Toast.makeText(LoginActivity.this, errMsg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
            Session.getInstance().clear();
            setProgress(false);
        }

        @Override
        protected void onCancelled() {
            if (DBG) Toast.makeText(LoginActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
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

        //запуск основной Activity
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
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
                return LoginActivity.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (DBG) Log.v(TAG, "sendOneTimePassword error", error);
                setProgress(false);
                if (error.getHttpStatus() >= 200 && error.getHttpStatus() < 300) {
                    // Ignore malformed JSON ("")
                    onSuccess(null, error.getRetrofitError().getResponse());
                } else {
                    CharSequence errText = error.getErrorDescription(getText(R.string.failed_to_send_one_time_password));
                    Toast toast = Toast.makeText(LoginActivity.this, errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                    setProgress(false);
                }
            }

            @Override
            protected void onCancelled() {
                if (DBG) Toast.makeText(LoginActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
                setProgress(false);
            }

            @Override
            protected void onSuccess(Void aVoid, Response response) {
                Toast toast = Toast.makeText(LoginActivity.this, getString(R.string.pass_sent,
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
        getSharedPreferences(APP_PREF, MODE_PRIVATE)
                .edit()
                .putStringSet(PREFS_KEY_LOGINS, newLogin)
                .apply();
    }

    private void loadLogins() {
        SharedPreferences preferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        Set<String> logins = preferences.getStringSet(PREFS_KEY_LOGINS, Collections.<String>emptySet());
        mLogins.addAll(logins);
    }

    //Проверка доступа в Инет
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }
}