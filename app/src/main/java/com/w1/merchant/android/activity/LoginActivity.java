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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.w1.merchant.android.request.JSONParsing;
import com.w1.merchant.android.request.POSTOtp;
import com.w1.merchant.android.request.POSTSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends Activity {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String APP_PREF = "W1_Pref";
    private static final long VIBRATE_TIME_MS = 100;
    private static final int ACT_MENU = 1;
    public static final String PREFS_KEY_LOGINS = "logins";

    private AutoCompleteTextView actvLogin;
    private View mAuthButton;
    private View tvForgot;
    private ImageView ivDelete;
    private EditText etPassword;
    private Vibrator mVibrator;

    public ProgressBar pbLogin;

    POSTSession postSession;
    POSTOtp postOtp;
    String[] requestData = {"", "", "", "", "", ""};
    String token, userId;

    private final Matcher mLooksLikePhoneMatcher = Pattern.compile("[0-9]{11}").matcher("");

    ArrayList<String> loginsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if (!isNetworkConnected()) {
            DialogFragment dlgNoInet = new DialogNoInet();
            dlgNoInet.show(getFragmentManager(), "dlgNoInet");
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        actvLogin = (AutoCompleteTextView) findViewById(R.id.actvLogin);
        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
        mAuthButton = findViewById(R.id.tvAuth);
        ivDelete = (ImageView) findViewById(R.id.ivDelete);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvForgot = findViewById(R.id.tvForgot);

        loadLogins();

        actvLogin.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, loginsArray));
        actvLogin.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                ivDelete.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                mAuthButton.setEnabled(isFormReadyToSend());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });

        mAuthButton.setOnClickListener(mOnClickListener);
        ivDelete.setOnClickListener(mOnClickListener); //значок удалить после логина

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tvForgot.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        //"Забыл"
        tvForgot.setOnClickListener(mOnClickListener);
        tvForgot.setVisibility(etPassword.hasFocus() ? View.VISIBLE : View.INVISIBLE);

        adjustBackgroundImageSizes();
        initScroller();

        if (BuildConfig.DEBUG && "debug".equals(BuildConfig.BUILD_TYPE) && !TextUtils.isEmpty(BuildConfig.API_TEST_USER)) {
            // Ибо заебал
            String req[] = new String[]{
                    Constants.URL_SESSION, BuildConfig.API_TEST_USER, BuildConfig.API_TEST_PASS
            };
            postSession = new POSTSession(LoginActivity.this);
            postSession.execute(req);
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
                    actvLogin.setText("");
                    break;
                case R.id.tvForgot:
                    mVibrator.vibrate(VIBRATE_TIME_MS);
                    //запрос капчи
                    //requestData[0] = Urls.URL + Urls.URL_CAPTCHA;
//				requestData[1] = "300";
//      			requestData[2] = "150";
//      			postCaptcha = new POSTCaptcha(activity);
//    			postCaptcha.execute(requestData);

                    //одноразовый пароль
                    requestData[0] = Constants.URL_OTP;
                    requestData[1] = actvLogin.getText().toString();
                    postOtp = new POSTOtp(LoginActivity.this);
                    postOtp.execute(requestData);
                    break;
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

    private void attemptLogin() {
        if (!validateForm()) return;

        mVibrator.vibrate(VIBRATE_TIME_MS);
        loginsArray.add(actvLogin.getText().toString());
        persistLogins();

        //создание сессии по логину и паролю
        requestData[0] = Constants.URL_SESSION;
        requestData[1] = actvLogin.getText().toString();
        requestData[2] = etPassword.getText().toString();
        postSession = new POSTSession(LoginActivity.this);
        postSession.execute(requestData);
    }

    //Проверка логина и пароля на пустоту
    public boolean validateForm() {
        boolean result;

        if (TextUtils.isEmpty(actvLogin.getText())) {
            actvLogin.setError(getText(R.string.error_field));
            result = false;
        } else result = true;
        if (TextUtils.isEmpty(etPassword.getText())) {
            if (result) etPassword.setError(getText(R.string.error_field));
            result = false;
        } else result = true;

        return result;
    }

    private boolean isFormReadyToSend() {
        String s = actvLogin.getText().toString();
        return s.contains("@") || mLooksLikePhoneMatcher.reset(s).matches();
    }

    private void persistLogins() {
        Set<String> newLogin = new HashSet<>();
        for (String n : loginsArray) newLogin.add(n);
        getSharedPreferences(APP_PREF, MODE_PRIVATE)
                .edit()
                .putStringSet(PREFS_KEY_LOGINS, newLogin)
                .apply();
    }

    private void loadLogins() {
        SharedPreferences preferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        Set<String> logins = preferences.getStringSet(PREFS_KEY_LOGINS, Collections.<String>emptySet());
        loginsArray.addAll(logins);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACT_MENU) {
            if (resultCode == RESULT_OK) {
                DialogFragment dlgSessionTimeout = new DialogTimeout();
                dlgSessionTimeout.show(getFragmentManager(), "dlgSessionTimeout");

            }
        }
    }

    public void sessionResult(String[] httpResult) {
        if (httpResult[0].equals("200")) {
            String idData[] = {"", "", ""};
            idData = JSONParsing.session(httpResult[1]);
            token = idData[1];//вместо Bearer после авторизации
            userId = idData[0];
            etPassword.setText("");
            actvLogin.setText("");
            tvForgot.setVisibility(View.INVISIBLE);
            Session.bearer = token;

            //запуск основной Activity
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("userId", userId);
            intent.putExtra("timeout", idData[2]);
            startActivityForResult(intent, ACT_MENU);
        } else if (httpResult[0].equals("400")) {
            Toast toast = Toast.makeText(this, getString(R.string.bad_password),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
        } else if (httpResult[0].equals("404")) {
            Toast toast = Toast.makeText(this, getString(R.string.user_not_found),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
        }
    }

    //Ответ на запрос одноразового пароля
    public void otpResult(String[] httpRes) {
        if (httpRes[0].equals("200")) {
            Toast toast = Toast.makeText(this, getString(R.string.pass_sent,
                    actvLogin.getText().toString()), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 50);
            toast.show();
        }
    }

    //Проверка доступа в Инет
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }
}

