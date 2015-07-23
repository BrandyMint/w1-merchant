package com.w1.merchant.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.AuthPrincipalRequest;
import com.w1.merchant.android.rest.model.PrincipalUser;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by alexey on 04.06.15.
 */
public class SelectPrincipalActivity extends ActivityBase implements SelectPrincipalFragment.InteractionListener {

    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    public static final String RESULT_PRINCIPAL = "com.w1.merchant.android.ui.SelectPrincipalActivity.RESULT_PRINCIPAL";

    public static final String RESULT_AUTH_USER = "com.w1.merchant.android.ui.SelectPrincipalActivity.RESULT_AUTH_USER";
    public static final String TAG_SELECT_PRINCIPAL_DIALOG = "SelectPrincipalDialog";

    private Subscription mAuthPrincipalSubscription = Subscriptions.unsubscribed();

    private Subscription mRequestPrincipalUsersSubscription = Subscriptions.unsubscribed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_principal);

        if (savedInstanceState == null) {
            SelectPrincipalFragment fragment = new SelectPrincipalFragment();
            fragment.show(getSupportFragmentManager(), TAG_SELECT_PRINCIPAL_DIALOG);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPrincipalUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthPrincipalSubscription.unsubscribe();
        mRequestPrincipalUsersSubscription.unsubscribe();
    }

    void requestPrincipalUsers() {
        mRequestPrincipalUsersSubscription.unsubscribe();

        Observable<List<PrincipalUser>> observer = AppObservable.bindActivity(this,
                RestClient.getApiMasterSessions().getPrincipalUsers());
        mRequestPrincipalUsersSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        refreshProgress();
                    }
                }).subscribe(new Observer<List<PrincipalUser>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(SelectPrincipalActivity.this, ((ResponseErrorException)e).getErrorDescription(
                                getText(R.string.error_auth_principal), getResources()), Toast.LENGTH_LONG).show();
                        if (DBG) Log.v(TAG, "selectPrincipal error", e);
                        finish();
                    }

                    @Override
                    public void onNext(List<PrincipalUser> principalUsers) {
                        if (principalUsers == null || principalUsers.isEmpty()) {
                            Toast.makeText(SelectPrincipalActivity.this, getText(R.string.error_you_have_only_one_principal), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            SelectPrincipalFragment fragment = (SelectPrincipalFragment)getSupportFragmentManager().findFragmentByTag(TAG_SELECT_PRINCIPAL_DIALOG);
                            if (fragment != null) {
                                fragment.setPrincipals(principalUsers);
                            } else {
                                finish();
                            }
                        }
                    }
                });
        refreshProgress();
    }

    void onPrincipalSelected(@Nullable PrincipalUser principal) {
        if (principal == null || TextUtils.equals(principal.principalUserId, Session.getInstance().getUserId())) {
            if (DBG) Log.v(TAG, "principal no changed");
            finish();
        } else {
            authPrincipal(principal);
        }
    }

    boolean isInProgress() {
        return !mAuthPrincipalSubscription.isUnsubscribed()
                || !mRequestPrincipalUsersSubscription.isUnsubscribed();
    }

    void refreshProgress() {
        SelectPrincipalFragment fragment = (SelectPrincipalFragment)getSupportFragmentManager().findFragmentByTag(TAG_SELECT_PRINCIPAL_DIALOG);
        if (fragment != null) fragment.setInProgress(isInProgress());
    }

    void authPrincipal(final PrincipalUser user) {
        mAuthPrincipalSubscription.unsubscribe();
        Observable<AuthModel> observer = AppObservable.bindActivity(this,
                RestClient.getApiMasterSessions().authPrincipal(new AuthPrincipalRequest(user.principalUserId)));

        mAuthPrincipalSubscription = observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this) {
                    @Override
                    protected void onInvalidToken() {
                        // Не показывам "сессия завершена", обработаем в onError
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        finish();
                    }
                })
                .subscribe(new Observer<AuthModel>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(SelectPrincipalActivity.this, ((ResponseErrorException)e).getErrorDescription(
                                getText(R.string.error_auth_principal), getResources()), Toast.LENGTH_LONG).show();
                        if (DBG) Log.v(TAG, "selectPrincipal error", e);
                    }

                    @Override
                    public void onNext(AuthModel authModel) {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_PRINCIPAL, user);
                        intent.putExtra(RESULT_AUTH_USER, authModel);
                        setResult(RESULT_OK, intent);
                    }
                });
        refreshProgress();
    }

    @Override
    public void onSelectPrincipalDialogDismissed(@Nullable PrincipalUser selectedUser) {
        onPrincipalSelected(selectedUser);
    }
}
