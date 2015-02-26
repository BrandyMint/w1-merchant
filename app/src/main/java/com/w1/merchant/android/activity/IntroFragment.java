package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
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
import com.w1.merchant.android.utils.FontManager;


public class IntroFragment extends DialogFragment {
    private static final String TAG = Constants.LOG_TAG;

    public static IntroFragment newInstance() {
        return new IntroFragment();
    }

    public IntroFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.IntroTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_intro, container, false);
        root.findViewById(R.id.banner_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final BannerInfinitePagerAdapter adapter;
        final InfiniteViewPager viewPager;
        final NewDotPageIndicator pagerIndicator;

        pagerIndicator = (NewDotPageIndicator)getView().findViewById(R.id.indicator_banner);
        viewPager = (InfiniteViewPager)getView().findViewById(R.id.pager_banner);
        adapter = new BannerInfinitePagerAdapter(0, viewPager, LayoutInflater.from(getView().getContext()));
        viewPager.setAdapter(adapter);
        pagerIndicator.setViewPager(viewPager, new String[] {"", "", "", ""});
        pagerIndicator.setChangePoint(0);
        pagerIndicator.setSnap(true);
        viewPager.setOnInfinitePageChangeListener(new InfiniteViewPagerListener(4) {
            @Override
            public void onPageSwitched(int position) {
                pagerIndicator.setChangePoint(position);
            }
        });
    }

    private static class BannerInfinitePagerAdapter extends InfinitePagerAdapter<Integer> {
        private final ViewGroup mRoot;
        private final LayoutInflater mLayoutInflater;

        /**
         * Standard constructor.
         *
         * @param initValue the initial indicator value the ViewPager should start with.
         */
        public BannerInfinitePagerAdapter(final Integer initValue, ViewGroup root, LayoutInflater inflater) {
            super(initValue);
            mRoot = root;
            mLayoutInflater = inflater;
        }

        @SuppressLint("InflateParams")
        @Override
        public ViewGroup instantiateItem(Integer indicator) {
            //int position = Math.abs(indicator) % 4;
            int position = indicator % 4;
            if (position < 0) {
                position = 4 + position;
            }

            ViewGroup root = (ViewGroup) mLayoutInflater.inflate(R.layout.banner1_layout, mRoot, false);
            ImageView img = (ImageView) root.findViewById(R.id.banner_img);
            TextView textTop = (TextView) root.findViewById(R.id.banner_text);
            textTop.setTypeface(FontManager.getInstance().getLightFont());

            int imgRes = 0;
            switch (position) {
                case 0:
                    imgRes = R.mipmap.intro1;
                    textTop.setText(R.string.text_banner_1);
                    break;
                case 1:
                    imgRes = R.mipmap.intro2;
                    textTop.setText(R.string.text_banner_2);
                    break;
                case 2:
                    imgRes = R.mipmap.intro3;
                    textTop.setText(R.string.text_banner_3);
                    break;
                case 3:
                    imgRes = R.mipmap.intro4;
                    textTop.setText(R.string.text_banner_4);
                default:
                    break;
            }
            if (imgRes != 0) {
                Picasso.with(root.getContext())
                        .load(imgRes)
                        .fit()
                        .centerInside()
                        .into(img);
            }
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
    }


}
