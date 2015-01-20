package ru.bokus.w1.Extra;

import java.util.ArrayList;

import ru.bokus.w1.ViewExtended.TextViewRobotoLight;
import ru.bokus.w1.ViewExtended.TextViewRouble;
import ru.bokus.w1.Activity.R;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

//адаптер для ViewPager с поступлениями за день, неделю, месяц
public class MainVPAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> mName;
    private ArrayList<String> mRubl;
    LayoutInflater mInflater;
 
    public MainVPAdapter(Context context, ArrayList<String> name,
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
        return view == ((RelativeLayout) object);
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
 
        TextViewRobotoLight tvName;
        TextViewRouble tvRubl;
        
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = mInflater.inflate(R.layout.fragment, container,
                false);
 
        tvName = (TextViewRobotoLight) itemView.findViewById(R.id.tvName);
        tvRubl = (TextViewRouble) itemView.findViewById(R.id.tvRubl);
        if (mRubl.get(position).equals("RUB")) { 
	        tvName.setText(mName.get(position));
	        tvRubl.setText("B"); 
        } else {
        	tvName.setText(mName.get(position) + " " + mRubl.get(position));
	        tvRubl.setText("");
        }
        
        ((ViewPager) container).addView(itemView);
 
        return itemView;
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}
