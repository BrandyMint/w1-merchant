package com.w1.merchant.android.extra;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.w1.merchant.android.R;

import java.util.ArrayList;
import java.util.List;

//адаптер для ViewPager с поступлениями за день, неделю, месяц
public class MainVPAdapter extends PagerAdapter {
    private final List<CharSequence> mValues;
 
    public MainVPAdapter() {
        this.mValues = new ArrayList<>();
    }

    public void setItems(List<CharSequence> items) {
        mValues.clear();
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mValues.size();
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextView textView = (TextView)LayoutInflater.from(container.getContext()).inflate(R.layout.currency_view_pager_2_item,
                container, false);

        textView.setText(mValues.get(position));
        container.addView(textView);
 
        return textView;
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
