package com.w1.merchant.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.AuthCreateModel;
import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.AuthPrincipalRequest;
import com.w1.merchant.android.rest.model.OneTimePassword;
import com.w1.merchant.android.rest.model.PrincipalUser;
import com.w1.merchant.android.rest.model.ResponseError;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

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

    // TODO переделать
    private final Matcher mLooksLikePhoneMatcher = Pattern.compile("[0-9]{11}").matcher("");

    private ArrayList<String> mLogins = new ArrayList<>();

    private String mLogin;

    private OnFragmentInteractionListener mListener;

    private Subscription mLoginSubscription = Subscriptions.unsubscribed();
    private Subscription mRequestPrincipalUsersSubscription = Subscriptions.unsubscribed();
    private Subscription mAuthPrincipalSubscription = Subscriptions.unsubscribed();
    private Subscription mSendOneTimePasswordSubscription = Subscriptions.unsubscribed();

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
        mLoginSubscription.unsubscribe();
        mRequestPrincipalUsersSubscription.unsubscribe();
        mAuthPrincipalSubscription.unsubscribe();
        mSendOneTimePasswordSubscription.unsubscribe();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private static CharSequence getMsgOneTimePasswordIsSend(CharSequence dest, Resources resources) {
        if (!TextUtilsW1.isPossibleEmail(dest) && TextUtilsW1.isPossiblePhoneNumber(dest)) {
            // TODO: 16.07.15 Форматировать телефонный номер?
            return resources.getString(R.string.sent_new_password_on_phone, dest);
        } else {
            return resources.getString(R.string.sent_new_password, dest);
        }
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

                    int below = rootHeight - authButtonLoc[1]; // расстояние под кнопкой "авторизоваться"
                    bottomView.setMaxHeight(below); // не залезаем на кнопку
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

            private Rect mRect = new Rect();

            @Override
            public void onGlobalLayout() {
                //r will be populated with the coordinates of your view that area still visible.
                activityRootView.getWindowVisibleDisplayFrame(mRect);
                int heightDiff = activityRootView.getRootView().getHeight() - (mRect.bottom - mRect.top);
                if (heightDiff > 300) { // if more than 300 pixels, its probably a keyboard...
                    if (!imeKeyboardShown) {
                        imeKeyboardShown = true;
                        final ScrollView scrollView = (ScrollView) root.findViewById(R.id.scroll_view);
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight());
                            }
                        }, 5 * 16);
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

    void actionOnError(Throwable e) {
        if (getActivity() == null) return;
        ResponseErrorException error = (ResponseErrorException)e;
        Toast toast;
        CharSequence errMsg;

        Session.getInstance().clear();

        if (error.isErrorCaptchaRequired() || error.isErrorInvalidCaptcha()) return;
        switch (error.getHttpStatus()) {
            case 400:
                if (error.error != null && ResponseError.ERROR_USER_PASSWORD_NOT_MATCH.equalsIgnoreCase(error.error.getTextCode())) {
                    // Показываем "Введён неверный пароль" вместо пришедшего с сервера "пароль пользователя не соответствует"
                    errMsg = getText(R.string.entered_password_incorrect);
                } else {
                    errMsg = error.getErrorDescription(getText(R.string.network_error), getResources());
                }
                break;
            case 404:
                errMsg = getText(R.string.user_not_found);
                break;
            default:
                errMsg = error.getErrorDescription(getText(R.string.network_error), getResources());
                break;
        }

        toast = Toast.makeText(getActivity(), errMsg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 50);
        toast.show();
    }

    private void attemptLogin(final String login, final String password) {
        mLogin = login;
        mLoginSubscription.unsubscribe();

        Observable<AuthModel> observer = AppObservable.bindFragment(this,
                RestClient.getApiMasterSessions().auth(new AuthCreateModel(login, password)));

        mLoginSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                        // Ничего не делаем, обработаем в onError
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setProgress(false);
                    }
                }).subscribe(new Observer<AuthModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "auth error", e);
                        actionOnError(e);
                    }

                    @Override
                    public void onNext(AuthModel response) {
                        if (DBG) Log.v(TAG, "auth success ");
                        Session.getInstance().setMasterAuth(response);
                        requestPrincipalUsers(response);
                    }
                });
        setProgress(true);
    }

    void requestPrincipalUsers(final AuthModel authModel) {
        mRequestPrincipalUsersSubscription.unsubscribe();

        Observable<List<PrincipalUser>> observer = AppObservable.bindFragment(this,
                RestClient.getApiMasterSessions().getPrincipalUsers());
        mRequestPrincipalUsersSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                        // Ничего не делаем, обработаем в onError
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setProgress(false);
                    }
                }).subscribe(new Observer<List<PrincipalUser>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "getPrincipalUsers error", e);
                        actionOnError(e);
                    }

                    @Override
                    public void onNext(List<PrincipalUser> principalUsers) {
                        if (principalUsers == null || principalUsers.isEmpty()) {
                            onAuthDone(authModel);
                        } else if (principalUsers.size() == 1) {
                            selectPrincipal(principalUsers.get(0));
                        } else {
                            showSelectPrincipalDialog(principalUsers);
                        }
                    }
                });
        setProgress(true);
    }

    void showSelectPrincipalDialog(List<PrincipalUser> principalUsers) {
        SelectPrincipalFragment fragment = new SelectPrincipalFragment() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                super.onDismiss(dialog);
                PrincipalUser user = getSelectedPrincipalUser();
                if (user != null) {
                    selectPrincipal(user);
                } else {
                    setProgress(false);
                    Session.getInstance().clear();
                }
            }
        };
        Bundle args = new Bundle(1);
        args.putParcelableArrayList(SelectPrincipalFragment.ARG_PRINCIPAL_USERS, new ArrayList<Parcelable>(principalUsers));
        fragment.setArguments(args);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment prev = getChildFragmentManager().findFragmentByTag("SelectPrincipalFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        fragment.show(ft, "SelectPrincipalFragment");
    }

    void selectPrincipal(final PrincipalUser user) {
        mAuthPrincipalSubscription.unsubscribe();
        Observable<AuthModel> observer = AppObservable.bindFragment(this,
                RestClient.getApiMasterSessions().authPrincipal(new AuthPrincipalRequest(user.principalUserId)));

        mAuthPrincipalSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                        // Ничего не делаем, обработаем в onError
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setProgress(false);
                    }
                })
                .subscribe(new Observer<AuthModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (DBG) Log.v(TAG, "selectPrincipal error", e);
                        actionOnError(e);
                    }

                    @Override
                    public void onNext(AuthModel authModel) {
                        onAuthDone(authModel);
                    }
                });
        setProgress(true);
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

        mSendOneTimePasswordSubscription.unsubscribe();

        Session.getInstance().clear();
        Observable<Void> observer = AppObservable.bindFragment(this,
                RestClient.getApiMasterSessions().sendOneTimePassword(new OneTimePassword.Request(login)));

        mSendOneTimePasswordSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                        // Ничего не делаем, обработаем в onError
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setProgress(false);
                    }
                })
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {
                        if (LoginFragment.this.getActivity() == null) return;
                        Toast toast = Toast.makeText(LoginFragment.this.getActivity(),
                                getMsgOneTimePasswordIsSend(mLoginTextView.getText(), getResources()),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                        setProgress(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseErrorException error = (ResponseErrorException)e;
                        if (DBG) Log.v(TAG, "sendOneTimePassword error", error);
                        if (error.getHttpStatus() >= 200 && error.getHttpStatus() < 300) {
                            // Ignore malformed JSON ("")
                            onCompleted();
                        } else {
                            if (LoginFragment.this.getActivity() == null) return;
                            CharSequence errText = error.getErrorDescription(getText(R.string.failed_to_send_one_time_password), getResources());
                            Toast toast = Toast.makeText(LoginFragment.this.getActivity(), errText, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 50);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNext(Void aVoid) {
                    }
                });
        setProgress(true);
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
