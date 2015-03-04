package com.w1.merchant.android.activity;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.msh.android.widget.view.NewDotPageIndicator;
import com.squareup.picasso.Picasso;
import com.thehayro.view.InfinitePagerAdapter;
import com.thehayro.view.InfiniteViewPager;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.InfiniteViewPagerListener;
import com.w1.merchant.android.model.PrincipalUser;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.viewextended.CircleTransformation;
import com.w1.merchant.android.viewextended.DefaultUserpicDrawable;

import java.util.Arrays;
import java.util.List;


public class SelectPrincipalFragment extends DialogFragment {

    public static final String ARG_PRINCIPAL_USERS = "com.w1.merchant.android.activity.SelectPrincipalFragment.ARG_PRINCIPAL_USERS";

    private static final String TAG = Constants.LOG_TAG;

    private List<PrincipalUser> mUsers;

    @Nullable
    private PrincipalUser mSelectedPrincipalUser = null;

    public SelectPrincipalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.SelectPrincipalDialogTheme);
        mUsers = getArguments().getParcelableArrayList(ARG_PRINCIPAL_USERS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final PrincipalInfinitePagerAdapter adapter;
        final InfiniteViewPager viewPager;
        final NewDotPageIndicator pagerIndicator;

        View root = inflater.inflate(R.layout.fragment_select_principal_user, container, false);
        pagerIndicator = (NewDotPageIndicator)root.findViewById(R.id.indicator_banner);
        viewPager = (InfiniteViewPager)root.findViewById(R.id.pager);
        adapter = new PrincipalInfinitePagerAdapter(mUsers, 0, viewPager, inflater);
        viewPager.setAdapter(adapter);

        String pagerViewContent[] = new String[mUsers.size()];
        Arrays.fill(pagerViewContent, "");
        pagerIndicator.setViewPager(viewPager, pagerViewContent);
        pagerIndicator.setChangePoint(0);
        pagerIndicator.setSnap(true);
        viewPager.setOnInfinitePageChangeListener(new InfiniteViewPagerListener(mUsers.size()) {
            @Override
            public void onPageSwitched(int position) {
                pagerIndicator.setChangePoint(position);
            }
        });

        return root;
    }

    @Nullable
    public PrincipalUser getSelectedPrincipalUser() {
        return mSelectedPrincipalUser;
    }

    void onPrincipalUserSelected(PrincipalUser user) {
        mSelectedPrincipalUser = user;
        dismissAllowingStateLoss();
    }

    private class PrincipalInfinitePagerAdapter extends InfinitePagerAdapter<Integer> {
        private final ViewGroup mRoot;
        private final LayoutInflater mLayoutInflater;

        private List<PrincipalUser> mUsers;

        /**
         * Standard constructor.
         *
         * @param initValue the initial indicator value the ViewPager should start with.
         */
        public PrincipalInfinitePagerAdapter(List<PrincipalUser> users, final Integer initValue, ViewGroup root, LayoutInflater inflater) {
            super(initValue);
            mUsers = users;
            mRoot = root;
            mLayoutInflater = inflater;
        }

        @SuppressLint("InflateParams")
        @Override
        public ViewGroup instantiateItem(Integer indicator) {
            int position = indicator % mUsers.size();
            if (position < 0) {
                position = mUsers.size() + position;
            }

            ViewGroup root = (ViewGroup) mLayoutInflater.inflate(R.layout.item_select_principal, mRoot, false);
            ImageView icon = (ImageView) root.findViewById(R.id.icon);
            TextView name = (TextView) root.findViewById(R.id.name);
            TextView accountId = (TextView)root.findViewById(R.id.account_id);
            TextView url = (TextView)root.findViewById(R.id.url);

            PrincipalUser user = mUsers.get(position);

            name.setText(user.title);
            accountId.setText(TextUtilsW1.formatUserId(user.principalUserId));
            url.setText(user.merchantUrl);

            int avatarDiameter = icon.getLayoutParams().width;
            DefaultUserpicDrawable defaultUserpicDrawable = new DefaultUserpicDrawable();
            defaultUserpicDrawable.setBounds(0, 0, avatarDiameter, avatarDiameter);
            defaultUserpicDrawable.setUser(user.title);

            if (!TextUtils.isEmpty(user.merchantLogo)) {
                Picasso.with(mRoot.getContext())
                        .load(user.merchantLogo)
                        .placeholder(R.drawable.avatar_dummy)
                        .error(defaultUserpicDrawable)
                        .transform(CircleTransformation.getInstance())
                        .into(icon);
            } else {
                Picasso.with(mRoot.getContext()).cancelRequest(icon);
                icon.setImageDrawable(defaultUserpicDrawable);
            }

            icon.setOnClickListener(mOnClickListener);
            icon.setTag(user);
            root.setTag(indicator);
            return root;
        }

        @Override
        public Integer getNextIndicator() {
            return getCurrentIndicator() + 1;
        }

        @Override
        public Integer getPreviousIndicator() {
            return getCurrentIndicator() - 1;
        }

        @Override
        public String getStringRepresentation(final Integer currentIndicator) {
            return String.valueOf(currentIndicator);
        }

        @Override
        public Integer convertToIndicator(final String representation) {
            return Integer.valueOf(representation);
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrincipalUser user = (PrincipalUser)v.getTag();
                onPrincipalUserSelected(user);
            }
        };
    }

}
