package com.w1.merchant.android.support;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.ActivityBase;
import com.w1.merchant.android.rest.model.SupportTicket;

public class TicketListActivity extends ActivityBase implements TicketListFragment.OnFragmentInteractionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "TicketListActivity";

    private static final int CREATE_TICKET_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME| ActionBar.DISPLAY_HOME_AS_UP| ActionBar.DISPLAY_USE_LOGO,
                ActionBar.DISPLAY_SHOW_HOME| ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_USE_LOGO);

        if (savedInstanceState == null) {
            Fragment fragment = TicketListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_TICKET_REQUEST) {
            if (resultCode == RESULT_OK) {
                SupportTicket ticket = data.getParcelableExtra(ConversationActivity.SUPPORT_TICKET_RESULT_KEY);
                TicketListFragment fragment = (TicketListFragment)getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.onTicketCreated(ticket);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartConversationClicked(View animateFrom) {
        if (DBG) Log.v(TAG, "onStartConversationClicked");
        startConversation(animateFrom);
    }

    @Override
    public void onOpenConversationClicked(View view, SupportTicket ticket) {
        if (DBG) Log.v(TAG, "onOpenConversationClicked" + ticket);
        ConversationActivity.startConversationActivity(this, ticket, view);
    }

    @Override
    public void notifyError(String error, Throwable exception) {
        if (exception != null) Log.e(TAG, error, exception);
        CharSequence errMsg;
        if (DBG) {
            errMsg = error + " " + (exception == null ? "" : exception.getLocalizedMessage());
        } else {
            errMsg = error;
        }
        Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
    }

    private void startConversation(@Nullable View animateFrom) {
        ConversationActivity.startActivityForResult(this, CREATE_TICKET_REQUEST, animateFrom);
    }
}
