package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.UserEntryAdapter;
import com.w1.merchant.android.extra.UserEntrySupport;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

public class UserEntryFragment extends Fragment {

    private View parentView;
    private ListView lvUserEntry;
    TextView tvAmount, tv, tvFooterText;
	RelativeLayout rlListItem;
	OnClickListener radioListener;
	UserEntryAdapter sAdapter;
	SegmentedRadioGroup srgUserEntry;
	private LinearLayout llFooter;
	UserEntrySupport userEntrySupport;
	MenuActivity menuActivity;
	Context context;
	String token;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.userentry, container, false);
        lvUserEntry = (ListView) parentView.findViewById(R.id.lvStatement);
        srgUserEntry = (SegmentedRadioGroup) parentView.findViewById(R.id.srgStatement);
        llFooter = (LinearLayout) inflater.inflate(R.layout.footer2, null);
        tvFooterText = (TextView) llFooter.findViewById(R.id.tvFooterText);
        context = getActivity();
        menuActivity = (MenuActivity) context;
        token = menuActivity.token;
        userEntrySupport = new UserEntrySupport(context);
        
        srgUserEntry.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    		@Override
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			lvUserEntry.removeFooterView(llFooter);
				menuActivity.currentPage = 1;
    			switch (checkedId) {
    			case R.id.rbEntrance:
    				userEntrySupport.getData("Inc", menuActivity.currentPage, "",
    						token, menuActivity.nativeCurrency);
    				menuActivity.filter = "Inc";
    				break;
    			case R.id.rbOutput:
    				userEntrySupport.getData("Out", menuActivity.currentPage, "",
    						token, menuActivity.nativeCurrency);
    				menuActivity.filter = "Out";
    				break;
    			}
    		}
    	});
        
        return parentView;
    }
	
	public void createListView() {
		//заполнение ListView
		if ((menuActivity.dataUserEntry.size() > 24) & 
				(lvUserEntry.getFooterViewsCount() == 0)) {
			lvUserEntry.addFooterView(llFooter);
		} else {
    		removeFooter();
    	}
				
		sAdapter = new UserEntryAdapter(context, menuActivity.dataUserEntry);
		lvUserEntry.setAdapter(sAdapter);
        
        lvUserEntry.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				rlListItem = (RelativeLayout) v;
				Intent intent = new Intent(getActivity(), Details.class);
				tv = (TextView) rlListItem.getChildAt(0);
				intent.putExtra("number", tv.getText().toString());
				tv = (TextView) rlListItem.getChildAt(1);
				intent.putExtra("date", tv.getText().toString());
				tv = (TextView) rlListItem.getChildAt(2);
				intent.putExtra("descr", tv.getText().toString());
				tv = (TextView) rlListItem.getChildAt(3);
				intent.putExtra("state", tv.getText().toString());
				tv = (TextView) rlListItem.getChildAt(4);
				intent.putExtra("amount", tv.getText().toString());
				intent.putExtra("currency", menuActivity.nativeCurrency);
				startActivity(intent);
			}
	    });
        
        lvUserEntry.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, 
					int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    // when list scrolling stops
                    manipulateWithVisibleViews(view);
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    break;
                case OnScrollListener.SCROLL_STATE_FLING:
                    break;
                }
			}
        });
	}
	
	private void manipulateWithVisibleViews(AbsListView view) {
        //int count = view.getChildCount(); // visible views count
        int lastVisibleItemPosition = view.getLastVisiblePosition();
        if ((lastVisibleItemPosition) == menuActivity.dataUserEntry.size()) {
        	menuActivity.currentPage += 1;
        	tvFooterText.setText(menuActivity.getString(R.string.loading));
        	userEntrySupport.getData(menuActivity.filter, 
        			menuActivity.currentPage, "", token, menuActivity.nativeCurrency);
        }
    }
	
	public void removeFooter() {
		lvUserEntry.removeFooterView(llFooter);
	}
	
	public void setHeaderText(String text) {
    	tvFooterText.setText(text);
    }
		
}
