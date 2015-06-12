package com.w1.merchant.android.ui.withdraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.ui.ActivityBase;
import com.w1.merchant.android.ui.adapter.WithdrawalGridAdapter;

/**
 * Список провайдеров
 */
public class ProviderListActivity extends ActivityBase
        implements ProviderListFragment.OnFragmentInteractionListener {

    private static final int CREATE_PROVIDER_REQUEST_CODE = Activity.RESULT_FIRST_USER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_list);

        if (savedInstanceState == null) {
            Fragment fragment = new ProviderListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_PROVIDER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else if (data != null && data.hasExtra(WithdrawActivity.RESULT_RESULT_TEXT)) {
                final String errText = data.getStringExtra(WithdrawActivity.RESULT_RESULT_TEXT);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || findViewById(R.id.content_frame) == null) return;
                        Snackbar.make(findViewById(R.id.content_frame), errText, Snackbar.LENGTH_LONG).show();
                    }
                }, 64);
            }
        }
    }

    @Override
    public void onProviderClicked(WithdrawalGridAdapter.ViewHolderItem holder, Provider provider) {
        WithdrawActivity.startCreateTemplateActivity(this,
                provider,
                CREATE_PROVIDER_REQUEST_CODE,
                holder.itemView, holder.title, holder.logo);
    }
}
