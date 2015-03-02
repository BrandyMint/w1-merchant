package com.w1.merchant.android.support;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.ActivityBase;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.viewextended.CircleTransformation;
import com.w1.merchant.android.viewextended.DefaultUserpicDrawable;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.app.ActionBar.DISPLAY_SHOW_HOME;
import static android.app.ActionBar.DISPLAY_USE_LOGO;

public class SupportProfileActivity extends ActivityBase {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;
    public static final String ARG_PROFILE = "com.w1.merchant.android.support.SupportProfileActivity.ARG_PROFILE";

    private ImageView mIconView;

    private TextView mNameView;

    private SupportTicketPost mPost;

    public static void startActivity(Context source, @Nullable SupportTicketPost post, @Nullable View animateFrom) {
        Intent intent = new Intent(source, SupportProfileActivity.class);
        intent.putExtra(ARG_PROFILE, post);
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
        setContentView(R.layout.activity_support_profile);

        mIconView = (ImageView)findViewById(R.id.icon);
        mNameView = (TextView)findViewById(R.id.name_value);

        mPost = getIntent().getParcelableExtra(ARG_PROFILE);

        if (mPost == null) throw new IllegalArgumentException();

        setupActionBar();
        setupProfile();
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

    private void setupActionBar() {
        ActionBar ab = getActionBar();
        if (ab == null) return;
        ab.setDisplayOptions(DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP| DISPLAY_USE_LOGO,
                DISPLAY_SHOW_HOME| DISPLAY_HOME_AS_UP|DISPLAY_USE_LOGO);
        ab.setTitle(mPost.userTitle);
        //ab.setIcon(android.R.color.transparent);
    }


    private void setupProfile() {
        mNameView.setText(mPost.userTitle);

        if (mIconView.getWidth() == 0) {
            mIconView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mIconView.getViewTreeObserver().isAlive()) {
                        mIconView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        setupAvatar();
                    }
                }
            });
        } else {
            setupAvatar();
        }
    }

    private void setupAvatar() {
        DefaultUserpicDrawable defaultUserpicDrawable;
        int avatarDiameter = mIconView.getWidth();
        String avatarUri = mPost.getAvatarUri(avatarDiameter);

        defaultUserpicDrawable = new DefaultUserpicDrawable();
        defaultUserpicDrawable.setBounds(0, 0, avatarDiameter, avatarDiameter);
        defaultUserpicDrawable.setUser(mPost.email);

        if (avatarUri != null) {
            Picasso.with(this)
                    .load(mPost.getAvatarUri(avatarDiameter))
                    .placeholder(R.drawable.avatar_dummy)
                    .error(defaultUserpicDrawable)
                    .transform(CircleTransformation.getInstance())
                    .into(mIconView);
        } else {
            mIconView.setImageDrawable(defaultUserpicDrawable);
        }
    }
}
