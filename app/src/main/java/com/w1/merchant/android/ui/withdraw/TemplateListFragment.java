package com.w1.merchant.android.ui.withdraw;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.Template;
import com.w1.merchant.android.ui.IProgressbarProvider;
import com.w1.merchant.android.ui.adapter.WithdrawalTemplateListAdapter;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import java.math.BigInteger;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Вывод по шаблонам, список шабонов
 */
public class TemplateListFragment extends Fragment {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final boolean FORCE_MODIFICATION_AVAILABLE = "debug".equals(BuildConfig.BUILD_TYPE);

    private GridView gridview;

    private TextView mEmptyListView;

    private OnFragmentInteractionListener mListener;

    private WithdrawalTemplateListAdapter mAdapter;

    private Subscription mGetTemplatesSubscription = Subscriptions.unsubscribed();

    private Subscription mDeleteTemplateSubscription = Subscriptions.unsubscribed();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_withdrawal_template_list, container, false);
        gridview = (GridView) parentView.findViewById(R.id.gridview);
        gridview.setOnItemClickListener(gridviewOnItemClickListener);
        mEmptyListView = (TextView)parentView.findViewById(R.id.empty_text);
        parentView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onCreateTemplateClicked(v);
            }
        });

        mAdapter = new WithdrawalTemplateListAdapter(getActivity());
        gridview.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                setupEmptyListStatus();
            }

            @Override
            public void onInvalidated() {
                setupEmptyListStatus();
            }
        });
        return parentView;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupFab();
        if (isTemplateModificationAllowed()) {
            registerForContextMenu(gridview);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadTemplates();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.template_list_item_actions, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editTemplate(info.targetView, info.position);
                return true;
            case R.id.delete:
                deleteTemplate(info.targetView, info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGetTemplatesSubscription.unsubscribe();
        mDeleteTemplateSubscription.unsubscribe();
        unregisterForContextMenu(gridview);
        gridview = null;
        mAdapter = null;
        mEmptyListView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean isTemplateModificationAllowed() {
        if (BuildConfig.DISABLE_TEMPLATE_CREATION) return false;
        if (FORCE_MODIFICATION_AVAILABLE) return true;
        if (mListener == null) {
            if (DBG) throw new IllegalStateException();
            return false;
        }

        if (mListener.isBusinessAccount()) {
            // Юрикам не разрешено создавать шаблоны ни при каких обстоятельствах
            return false;
        } else {
            // Для физиков считаем, что можно, если аккаунт идентифицирован.
            return mListener.isMerchantVerified();
        }
    }

    private void setupFab() {
        if (mListener == null || getView() == null) return;
        View fab = getView().findViewById(R.id.fab);
        fab.setVisibility(isTemplateModificationAllowed() ? View.VISIBLE : View.GONE);
    }

    private void setupEmptyListStatus() {
        if (mEmptyListView == null || mListener == null) return;

        if (!mGetTemplatesSubscription.isUnsubscribed()
                || !mAdapter.isEmpty()
                ) {
            mEmptyListView.setVisibility(View.INVISIBLE);
            return;
        }

        mEmptyListView.setVisibility(View.VISIBLE);
        if (isTemplateModificationAllowed()) {
            mEmptyListView.setText(R.string.no_withdrawal_templates);
        } else {
            if (mListener.isBusinessAccount()) {
                mEmptyListView.setText(R.string.to_withdraw_funds_you_need_to_sign_a_contract);
            } else {
                mEmptyListView.setText(R.string.to_withdraw_funds_you_need_to_undergo_identification);
            }
        }
    }

    private void reloadTemplates() {
        if (mListener == null) return;
        mGetTemplatesSubscription.unsubscribe();

        Observable<Template.TempateList> observable = AppObservable.bindFragment(this,
                RestClient.getApiPayments().getTemplates());

        NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);
        mGetTemplatesSubscription = observable
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(stopProgressAction)
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        setupEmptyListStatus();
                    }
                })
                .subscribe(new Observer<Template.TempateList>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showError(getText(R.string.network_error), e);
                    }

                    @Override
                    public void onNext(Template.TempateList templateList) {
                        mAdapter.setTemplates(templateList.items);
                    }
                });
        stopProgressAction.token = mListener.startProgress();
    }

    private void deleteTemplate(View view, int position) {
        if (DBG) Log.v(TAG, "deleteTemplate " + position);
        Template template = (Template)gridview.getItemAtPosition(position);
        if (template == null) return;

        mDeleteTemplateSubscription.unsubscribe();

        final BigInteger templateId = template.templateId;
        Observable<Void> observable = RestClient.getApiPayments().deleteTemplate(templateId.toString());
        NetworkUtils.StopProgressAction stopProgressAction = new NetworkUtils.StopProgressAction(mListener);
        stopProgressAction.token = mListener.startProgress();
        mDeleteTemplateSubscription = AppObservable.bindFragment(this, observable)
                .subscribeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .doOnUnsubscribe(stopProgressAction)
                .subscribe(new Observer<Void>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseErrorException error = (ResponseErrorException)e;
                        if (error.getHttpStatus() >= 200 && error.getHttpStatus() < 300) {
                            // Ignore malformed JSON ("")
                            mAdapter.deleteTemplate(templateId);
                            return;
                        }
                        showError(getText(R.string.network_error), e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        mAdapter.deleteTemplate(templateId);
                    }
                });
    }

    private void editTemplate(View view, int position) {
        if (DBG) Log.v(TAG, "editTemplate " + position);
        Template template = (Template)gridview.getItemAtPosition(position);
        if (template == null) return;
        if (mListener != null) mListener.onEditTemplateClicked(view,
                template,
                view == null ? null : view.findViewById(R.id.textpart),
                view == null ? null : view.findViewById(R.id.imagepart));
    }

    private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            Template template = (Template)parent.getItemAtPosition(position);
            if (template == null) {
                Uri address = Uri.parse(Constants.URL_W1_MERCHANT);
                startActivity(new Intent(Intent.ACTION_VIEW, address));
            } else {
                Intent intent = new Intent(getActivity(), WithdrawByTemplateActivity.class);
                intent.putExtra("templateId", String.valueOf(template.templateId));
                intent.putExtra("token", Session.getInstance().getAuthtoken());
                intent.putExtra("mIsBusinessAccount", mListener.isBusinessAccount());
                startActivity(intent);
            }
        }
    };

    private void showError(CharSequence defaultErrorDescription, Throwable throwable) {
        CharSequence errText;
        if (throwable instanceof ResponseErrorException) {
            errText = ((ResponseErrorException) throwable).getErrorDescription(getText(R.string.network_error), getResources());
        } else {
            errText = defaultErrorDescription;
        }
        Snackbar.make(getView(), errText, Snackbar.LENGTH_LONG).show();
    }

    public interface OnFragmentInteractionListener extends IProgressbarProvider {
        boolean isBusinessAccount();
        boolean isMerchantVerified();
        void onCreateTemplateClicked(View animateFrom);
        void onEditTemplateClicked(View view, Template template, View titleViewFrom, View imageViewForm);
    }

}
