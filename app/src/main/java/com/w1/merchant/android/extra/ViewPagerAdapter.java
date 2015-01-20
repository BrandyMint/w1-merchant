package com.w1.merchant.android.extra;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.MenuActivity;
import com.w1.merchant.android.viewextended.TextViewRobotoLight;
import com.w1.merchant.android.viewextended.TextViewRouble;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> mName;
    private ArrayList<String> mRubl;
    LayoutInflater mInflater;
 
    public ViewPagerAdapter(Context context, ArrayList<String> name,
    		ArrayList<String> counter) {
        this.mContext = context;
        this.mName = name;
        this.mRubl = counter;
    }
 
    @Override
    public int getCount() {
        return mName.size();
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
 
        TextViewRobotoLight tvName;
        TextViewRouble tvRubl;
        
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = mInflater.inflate(R.layout.viewpager, container,
                false);
 
        tvName = (TextViewRobotoLight) itemView.findViewById(R.id.tvName);
        tvRubl = (TextViewRouble) itemView.findViewById(R.id.tvRubl);
        tvName.setText(mName.get(position));
        tvRubl.setText(mRubl.get(position));
        tvName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!((MenuActivity) mContext).waitSum.isEmpty()) {
				if (((MenuActivity) mContext).getCurrentFragment()
						== MenuActivity.FRAGMENT_DASH) {
					Toast toast = Toast.makeText(mContext, 
							mContext.getString(R.string.awaiting) + " " +
							((MenuActivity) mContext).getWaitSum(),
							Toast.LENGTH_LONG);
			    	toast.setGravity(Gravity.TOP, 0, 100);
			    	toast.show();
				}
				}
			}
		});
        
        container.addView(itemView);
 
        return itemView;
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
