package com.w1.merchant.android.support;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;

public class CreateTicketActivity extends Activity implements CreateTicketFragment.OnFragmentInteractionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    public static final String SUPPORT_TICKET_RESULT_KEY = "com.w1.merchant.android.support.CreateTicketActivity.SUPPORT_TICKET_RESULT_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ticket);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, CreateTicketFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void onSupportTicketCreated(SupportTicket ticket) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(SUPPORT_TICKET_RESULT_KEY, ticket);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void notifyError(CharSequence error, @Nullable Throwable exception) {
        if (exception != null) Log.e(TAG, error.toString(), exception);
        CharSequence errMsg;
        if (DBG) {
            errMsg = error + " " + (exception == null ? "" : exception.getLocalizedMessage());
        } else {
            errMsg = error;
        }
        Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
    }
}
