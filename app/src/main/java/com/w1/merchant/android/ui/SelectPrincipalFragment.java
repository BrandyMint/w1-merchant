package com.w1.merchant.android.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
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
import com.w1.merchant.android.rest.model.PrincipalUser;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.CircleTransformation;
import com.w1.merchant.android.ui.widget.DefaultUserpicDrawable;

import java.util.Arrays;
import java.util.List;

/**
 * Диалог выбора аккаунта мерчанта
 */
public class SelectPrincipalFragment extends DialogFragment {

    public static final String ARG_PRINCIPAL_USERS = "com.w1.merchant.android.ui.SelectPrincipalFragment.ARG_PRINCIPAL_USERS";

    private static final String TAG = Constants.LOG_TAG;

    private NewDotPageIndicator mPagerIndicator;

    private InfiniteViewPager mViewPager;

    private View mProgressView;

    @Nullable
    private PrincipalUser mSelectedPrincipalUser = null;

    private InteractionListener mListener;

    public interface InteractionListener {
        void onSelectPrincipalDialogDismissed(@Nullable PrincipalUser selectedUser);
    }

    public SelectPrincipalFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getParentFragment() != null && getParentFragment() instanceof InteractionListener) {
            mListener = (InteractionListener)getParentFragment();
        } else if (activity instanceof InteractionListener) {
            mListener = (InteractionListener)activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.SelectPrincipalDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_select_principal_user, container, false);
        mPagerIndicator = (NewDotPageIndicator)root.findViewById(R.id.indicator_banner);
        mViewPager = (InfiniteViewPager)root.findViewById(R.id.pager);
        mProgressView = root.findViewById(R.id.progress);

        if (getArguments() != null && getArguments().containsKey(ARG_PRINCIPAL_USERS)) {
            List<PrincipalUser> users = getArguments().getParcelableArrayList(ARG_PRINCIPAL_USERS);
            setPrincipals(users);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof SelectPrincipalActivity) {
            setInProgress(((SelectPrincipalActivity) getActivity()).isInProgress());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) mListener.onSelectPrincipalDialogDismissed(mSelectedPrincipalUser);
    }

    public void setInProgress(boolean inProgress) {
        if (mProgressView != null) mProgressView.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    public void setPrincipals(List<PrincipalUser> users) {
        final PrincipalInfinitePagerAdapter adapter;
        adapter = new PrincipalInfinitePagerAdapter(users, 0, mViewPager, LayoutInflater.from(mViewPager.getContext()));
        mViewPager.setAdapter(adapter);

        String pagerViewContent[] = new String[users.size()];
        Arrays.fill(pagerViewContent, "");
        mPagerIndicator.setViewPager(mViewPager, pagerViewContent);
        mPagerIndicator.setChangePoint(0);
        mPagerIndicator.setSnap(true);
        mViewPager.setOnInfinitePageChangeListener(new InfiniteViewPagerListener(users.size()) {
            @Override
            public void onPageSwitched(int position) {
                mPagerIndicator.setChangePoint(position);
            }
        });
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
