package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.w1.merchant.android.model.Template;
import com.w1.merchant.android.service.ApiPayments;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.utils.NetworkUtils;

import retrofit.Callback;
import retrofit.client.Response;

public class TemplateFragment extends Fragment {

    private ApiPayments mApiPayments;
    private GridView gridview;

    private OnFragmentInteractionListener mListener;

    private ImageTextAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiPayments = NetworkUtils.getInstance().createRestAdapter().create(ApiPayments.class);
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
        mAdapter = null;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void reloadTemplates() {
        if (mListener == null) return;
        mListener.startProgress();
        new ApiRequestTask<Template.TempateList>() {

            @Override
            protected void doRequest(Callback<Template.TempateList> callback) {
                mApiPayments.getTemplates(callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return getActivity();
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                if (mListener != null) {
                    mListener.stopProgress();
                    CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                    Toast toast = Toast.makeText(getActivity(), errText, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 50);
                    toast.show();
                }
            }

            @Override
            protected void onCancelled() {
                if (mListener != null) {
                    mListener.stopProgress();
                }
            }

            @Override
            protected void onSuccess(Template.TempateList tempateList, Response response) {
                if (mListener == null) return;
                mListener.stopProgress();
                mAdapter.setTemplates(tempateList.items);
                if (gridview.getAdapter() != mAdapter) gridview.setAdapter(mAdapter);
            }
        }.execute();
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
                intent.putExtra("token", Session.getInstance().getBearer());
                intent.putExtra("mIsBusinessAccount", mListener.isBusinessAccount());
                startActivity(intent);
            }
        }
    };

    public interface OnFragmentInteractionListener {
        public boolean isBusinessAccount();
        public void startProgress();
        public void stopProgress();

    }
}
