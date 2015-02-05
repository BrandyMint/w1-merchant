package com.w1.merchant.android.support;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.app.ActionBar.DISPLAY_SHOW_HOME;
import static android.app.ActionBar.DISPLAY_USE_LOGO;

public class TicketListActivity extends Activity implements TicketListFragment.OnFragmentInteractionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "TicketListActivity";

    private static final int CREATE_TICKET_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);

        getActionBar().setDisplayOptions(DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP| DISPLAY_USE_LOGO,
                DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP|DISPLAY_USE_LOGO);

        if (savedInstanceState == null) {
            Fragment fragment = TicketListFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_TICKET_REQUEST) {
            if (resultCode == RESULT_OK) {
                SupportTicket ticket = data.getParcelableExtra(CreateTicketActivity.SUPPORT_TICKET_RESULT_KEY);
                TicketListFragment fragment = (TicketListFragment)getFragmentManager().findFragmentById(R.id.container);
                fragment.onTicketCreated(ticket);
                ConversationActivity.startConversationActivity(this, ticket, null);
                overridePendingTransition(0, 0);
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
    public void onStartConversationClicked() {
        if (DBG) Log.v(TAG, "onStartConversationClicked");
        startConversation();
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

    private void startConversation() {
        Intent intent = new Intent(this, CreateTicketActivity.class);
        startActivityForResult(intent, CREATE_TICKET_REQUEST);
    }
}
