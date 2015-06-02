package com.w1.merchant.android.extra;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.model.Captcha;
import com.w1.merchant.android.service.ApiSessions;
import com.w1.merchant.android.utils.NetworkUtils;

import rx.Observable;
import rx.Observer;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;

public class CaptchaDialogFragment extends DialogFragment {

    public static final String ACTION_DIALOG_NEW_STATUS = "com.w1.merchant.android.extra.CaptchaDialogFragment.ACTION_DIALOG_NEW_STATUS";

    public static final int STATUS_SHOWN = 1;

    public static final int STATUS_CANCELLED = 2;

    public static final int STATUS_DONE = 3;

    private static final String ARG_SHOW_INVALID_CODE = "com.w1.merchant.android.extra.CaptchaDialogFragment.ARG_SHOW_INVALID_CODE";

    private ImageView mImageView;

    private EditText mCodeView;

    private View mProgress;

    private Captcha mCaptcha;

    private boolean isDone;

    private boolean mInProgress;

    public static CaptchaDialogFragment newInstance(boolean showInvalidCode) {
        CaptchaDialogFragment fragment = new CaptchaDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(ARG_SHOW_INVALID_CODE, showInvalidCode);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.fragment_captcha_dialog, null);

        mImageView = (ImageView) root.findViewById(R.id.captcha_image);
        mCodeView = (EditText) root.findViewById(R.id.captcha_code);
        mProgress = root.findViewById(R.id.progress);

        builder
                //.setTitle(R.string.enter_captcha_dialog_title)
                .setView(root)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.refresh, null)
                .setNegativeButton(android.R.string.cancel, null);

        mCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSetCaptchaCodeClicked();
                }
                return true;
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSetCaptchaCodeClicked();
                    }
                });
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recreateCaptcha();
                    }
                });
            }
        });
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isDone) onDialogDismissed();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (!isDone) onDialogDismissed();
    }

    @Override
    public void onStart() {
        super.onStart();
        notifyDialogShown();
        if (getArguments().getBoolean(ARG_SHOW_INVALID_CODE)) {
            String captchaCode;
            synchronized (Session.class) {
                mCaptcha = Session.getInstance().captcha;
                captchaCode = Session.getInstance().captchaCode;
            }
            if (mCaptcha == null) {
                recreateCaptcha();
            } else {
                setInProgress(true);
                mCodeView.setText(captchaCode);
                mCodeView.setSelection(captchaCode.length());
                Toast.makeText(getActivity(), R.string.error_captcha_wrong_code, Toast.LENGTH_LONG).show();
                Picasso.with(getActivity())
                        .load(mCaptcha.captchaUrl)
                        .fit()
                        .centerInside()
                        .into(mImageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                setInProgress(false);
                            }

                            @Override
                            public void onError() {
                                recreateCaptcha();
                            }
                        });
            }
        } else {
            recreateCaptcha();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeView.post(new Runnable() {
            @Override
            public void run() {
                mCodeView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(mCodeView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isDone) dismissAllowingStateLoss(); // В любой непонятной ситуации отменяем всё нафиг
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mImageView = null;
        mCodeView = null;
        mProgress = null;
    }

    private void notifyDialogShown() {
        Intent intent = new Intent(ACTION_DIALOG_NEW_STATUS);
        intent.putExtra(ACTION_DIALOG_NEW_STATUS, STATUS_SHOWN);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void onDialogDismissed() {
        Session session = Session.getInstance();
        synchronized (Session.class) {
            session.captcha = null;
            session.captchaCode = null;
        }
        Intent intent = new Intent(ACTION_DIALOG_NEW_STATUS);
        intent.putExtra(ACTION_DIALOG_NEW_STATUS, STATUS_CANCELLED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void notifyDialogDone() {
        Intent intent = new Intent(ACTION_DIALOG_NEW_STATUS);
        intent.putExtra(ACTION_DIALOG_NEW_STATUS, STATUS_DONE);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    void onSetCaptchaCodeClicked() {
        if (mCodeView == null) return;

        if (TextUtils.isEmpty(mCodeView.getText()) || mCaptcha == null) {
            Toast.makeText(getActivity(), R.string.enter_captcha_error, Toast.LENGTH_LONG).show();
            return;
        }

        Session session = Session.getInstance();
        synchronized (Session.class) {
            session.captchaCode = mCodeView.getText().toString();
            session.captcha = mCaptcha;
        }
        isDone = true;
        notifyDialogDone();
        dismissAllowingStateLoss();
    }

    private void setInProgress(boolean inProgress) {
        mInProgress = inProgress;
        if (mProgress != null) {
            mProgress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        }
    }

    private void recreateCaptcha() {
        if (mInProgress) return;
        setInProgress(true);

        ApiSessions api = NetworkUtils.getInstance().createRestAdapter().create(ApiSessions.class);

        Captcha.CaptchaRequest req = new Captcha.CaptchaRequest(
                getResources().getDimensionPixelSize(R.dimen.captcha_width),
                getResources().getDimensionPixelSize(R.dimen.captcha_height));

        Observable<Captcha> observer = AppObservable.bindFragment(this,
                api.createCaptchaCode(req));
        observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Captcha>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissAllowingStateLoss();
                    }

                    @Override
                    public void onNext(Captcha captcha) {
                        if (getActivity() == null || mImageView == null) return;
                        mCaptcha = captcha;
                        Picasso.with(getActivity())
                                .load(captcha.captchaUrl)
                                .fit()
                                .centerInside()
                                .into(mImageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        setInProgress(false);
                                    }

                                    @Override
                                    public void onError() {
                                        dismissAllowingStateLoss();
                                    }
                                });
                    }
                });
    }

}
