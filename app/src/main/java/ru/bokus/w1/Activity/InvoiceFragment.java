package ru.bokus.w1.Activity;

import java.util.ArrayList;
import java.util.Map;

import ru.bokus.w1.Extra.InvoiceSupport;
import ru.bokus.w1.Extra.UserEntryAdapter;
import ru.bokus.w1.ViewExtended.SegmentedRadioGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class InvoiceFragment extends Fragment {

    private View parentView;
    private ListView lvInvoice;
    ArrayList<String> numberArray, dateArray, amountArray;
    ArrayList<Integer> imgArray;
	ArrayList<Map<String, Object>> data;
	Map<String, Object> m;
	SimpleAdapter sAdapter;
	TextView tvAmount, tvFooterText, tv;
	Context context;
	MenuActivity menuActivity;
	SegmentedRadioGroup srgInvoice;
	private LinearLayout llFooter;
	InvoiceSupport invoiceSupport;
	SwipeRefreshLayout srlInvoice;
	SearchView svList;
	String token;
	RelativeLayout rlListItem;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.invoices, container, false);
        context = getActivity();
        menuActivity = (MenuActivity) context;
        token = menuActivity.token;
        srgInvoice = (SegmentedRadioGroup) parentView.findViewById(R.id.srgInvoice);
        lvInvoice = (ListView) parentView.findViewById(R.id.lvAccounts);
        llFooter = (LinearLayout) inflater.inflate(R.layout.footer2, null);
        invoiceSupport = new InvoiceSupport(context);
        tvFooterText = (TextView) llFooter.findViewById(R.id.tvFooterText);
        
        srgInvoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    		@Override
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			lvInvoice.removeFooterView(llFooter);
    			menuActivity.currentPage = 1;
    			switch (checkedId) {
    			case R.id.rbPaid:
    				invoiceSupport.getData("Accepted", menuActivity.currentPage, "", token);
    				menuActivity.filter = "Accepted";
        			break;
    			case R.id.rbNotPaid:
    				invoiceSupport.getData("Created", menuActivity.currentPage, "", token);
    				menuActivity.filter = "Created";
    				break;
    			case R.id.rbPartially:
    				invoiceSupport.getData("HasSuspense", menuActivity.currentPage, "", token);
    				menuActivity.filter = "HasSuspense";
    				break;
    			}
    		}
    	});
        return parentView;
    }
    
    void createListView() {
    	//заполнение ListView
    	if ((menuActivity.dataInvoice.size() > 24) & 
    			(lvInvoice.getFooterViewsCount() == 0)) {
    		lvInvoice.addFooterView(llFooter);
    	} else {
    		removeFooter();
    	}
    	/*String[] from = { menuActivity.ATTRIBUTE_NAME_NUMBER, menuActivity.ATTRIBUTE_NAME_DATE,
        		menuActivity.ATTRIBUTE_NAME_IMAGE, menuActivity.ATTRIBUTE_NAME_AMOUNT,
        		menuActivity.ATTRIBUTE_NAME_RUBL, menuActivity.ATTRIBUTE_NAME_DESCR};
		int[] to = { R.id.tvNumber, R.id.tvDate, 
		    		R.id.ivIcon, R.id.tvAmount, R.id.tvRubl, R.id.tvDescr };*/
		
		sAdapter = new UserEntryAdapter(context, menuActivity.dataInvoice, 
				R.layout.invoice_item, menuActivity.from, menuActivity.to);
		lvInvoice.setAdapter(sAdapter);
		lvInvoice.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                }
			}
        });
		
		lvInvoice.setOnItemClickListener(new OnItemClickListener() {
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
		lvInvoice.setTextFilterEnabled(true);
    }
    
    private void manipulateWithVisibleViews(AbsListView view) {
        int lastVisibleItemPosition = view.getLastVisiblePosition();
        if ((lastVisibleItemPosition) == menuActivity.dataInvoice.size()) {
        	menuActivity.currentPage += 1;
        	tvFooterText.setText(menuActivity.getString(R.string.loading));
        	invoiceSupport.getData(menuActivity.filter,	menuActivity.currentPage, "", token);
        }
    }
    
    public void removeFooter() {
		lvInvoice.removeFooterView(llFooter);
	}
    
    public void setHeaderText(String text) {
    	tvFooterText.setText(text);
    }
}
