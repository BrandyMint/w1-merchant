package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.ImageTextAdapter;
import com.w1.merchant.android.rest.model.Template;
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class TemplateFragment extends Fragment {

    private GridView gridview;

    private OnFragmentInteractionListener mListener;

    private ImageTextAdapter mAdapter;

    private Subscription mGetTemplatesSubscription = Subscriptions.unsubscribed();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.templates, container, false);
        gridview = (GridView) parentView.findViewById(R.id.gridview);
        gridview.setOnItemClickListener(gridviewOnItemClickListener);
        mAdapter = new ImageTextAdapter(getActivity());
        gridview.setAdapter(mAdapter);
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
    public void onStart() {
        super.onStart();
        reloadTemplates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGetTemplatesSubscription.unsubscribe();
        mAdapter = null;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                .subscribe(new Observer<Template.TempateList>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        CharSequence errText = ((ResponseErrorException)e).getErrorDescription(getText(R.string.network_error), getResources());
                        Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(Template.TempateList tempateList) {
                        mAdapter.setTemplates(tempateList.items);
                        if (gridview.getAdapter() != mAdapter) gridview.setAdapter(mAdapter);
                    }
                });
        stopProgressAction.token = mListener.startProgress();
    }

    private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            Template template = (Template)parent.getItemAtPosition(position);
            if (template == null) {
                Uri address = Uri.parse(Constants.URL_WALLETONE);
                startActivity(new Intent(Intent.ACTION_VIEW, address));
            } else {
                Intent intent = new Intent(getActivity(), EditTemplate.class);
                intent.putExtra("templateId", String.valueOf(template.templateId));
                intent.putExtra("token", Session.getInstance().getAuthtoken());
                intent.putExtra("mIsBusinessAccount", mListener.isBusinessAccount());
                startActivity(intent);
            }
        }
    };

    public interface OnFragmentInteractionListener extends IProgressbarProvider{
        public boolean isBusinessAccount();
    }
}
