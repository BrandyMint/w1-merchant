package com.w1.merchant.android.support;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.app.ActionBar.DISPLAY_SHOW_HOME;
import static android.app.ActionBar.DISPLAY_USE_LOGO;

public class ConversationActivity extends Activity implements ConversationFragment.OnFragmentInteractionListener {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String ARG_TICKET = "com.w1.merchant.android.support.ConversationActivity.ARG_TICKET";

    private boolean imeKeyboardShown;

    private SupportTicket mTicket;

    public static void startConversationActivity(Context source, SupportTicket ticket, View animateFrom) {
        Intent intent = new Intent(source, ConversationActivity.class);
        intent.putExtra(ARG_TICKET, ticket);
        if (animateFrom != null && source instanceof Activity) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                    animateFrom, 0, 0, animateFrom.getWidth(), animateFrom.getHeight());
            ActivityCompat.startActivity((Activity) source, intent, options.toBundle());
        } else {
            source.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mTicket = getIntent().getParcelableExtra(ARG_TICKET);
        if (mTicket == null) throw new IllegalArgumentException("Ticket not defined");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ConversationFragment.newInstance(mTicket))
                    .commit();
        }

        setupActionBar();

        final View activityRootView = findViewById(R.id.container);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!imeKeyboardShown) {
                        imeKeyboardShown = true;
                        onImeKeyboardShown();
                    }
                } else {
                    if (imeKeyboardShown) {
                        imeKeyboardShown = false;
                        onImeKeyboardHidden();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void notifyError(CharSequence error, @Nullable Throwable exception) {
        if (DBG && exception != null) Log.e(TAG, error.toString(), exception);
        if (DBG) {
            error = error.toString() + " " + (exception == null ? "" : exception.getLocalizedMessage());
        }
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    void onImeKeyboardShown() {
        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) fragment.onImeKeyboardShown();
    }

    void onImeKeyboardHidden() {

    }

    private void setupActionBar() {
        ActionBar ab = getActionBar();
        if (ab == null) return;
        ab.setDisplayOptions(DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP| DISPLAY_USE_LOGO,
                DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP|DISPLAY_USE_LOGO);
        ab.setTitle(mTicket.ticketMask);
    }

}
