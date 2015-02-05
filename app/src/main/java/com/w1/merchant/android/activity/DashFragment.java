package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.DashSupport;
import com.w1.merchant.android.extra.MainVPAdapter;
import com.w1.merchant.android.extra.MyMarkerView;
import com.w1.merchant.android.extra.UserEntryAdapter;
import com.w1.merchant.android.viewextended.SegmentedRadioGroup;

import java.util.ArrayList;

public class DashFragment extends Fragment {

    private View parentView;
    
    public ListView lvDash;
    TextView tv, tvFooterText, tvPercent;
	RelativeLayout rlListItem;
	UserEntryAdapter sAdapter;
	Context context;
	ViewPager vpDash;
	PagerAdapter pagerAdapter;
	SegmentedRadioGroup srgDash;
	RadioButton rbHour, rbWeek, rbMonth;
	private LinearLayout llFooter, llHeader;
	DashSupport dashSupport;
	MenuActivity menuActivity;
	//XYSeries series1;
	SwipeRefreshLayout swipeLayout;
	LineChart mChart;
	ImageView ivPercent;
	
	@SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	parentView = inflater.inflate(R.layout.dashboard, container, false);
    	llFooter = (LinearLayout) inflater.inflate(R.layout.footer2, null);
    	llHeader = (LinearLayout) inflater.inflate(R.layout.dash_header, null);
    	//llPlot = (LinearLayout) llHeader.findViewById(R.id.llPlot);
    	mChart = (LineChart) llHeader.findViewById(R.id.chart1);
    	tvPercent = (TextView) llHeader.findViewById(R.id.tvPercent);
    	ivPercent = (ImageView) llHeader.findViewById(R.id.ivPercent);
    	tvFooterText = (TextView) llFooter.findViewById(R.id.tvFooterText);
    	context = getActivity();
    	dashSupport = new DashSupport(context);
    	setUpViews();
        return parentView;
    }

    private void setUpViews() {
        menuActivity = (MenuActivity) getActivity();
        lvDash = (ListView) parentView.findViewById(R.id.lvDash);
        srgDash = (SegmentedRadioGroup) llHeader.findViewById(R.id.srgDash);
        rbHour = (RadioButton) llHeader.findViewById(R.id.rbHour);
        rbHour.setChecked(true);
        rbWeek = (RadioButton) llHeader.findViewById(R.id.rbWeek);
        rbMonth = (RadioButton) llHeader.findViewById(R.id.rbMonth);
        swipeLayout = (SwipeRefreshLayout) parentView;
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				rbHour.setChecked(true);
				//menuActivity.getBalance();
				menuActivity.selectItem(1);
			}
		});        
    }
    
    public void createListView() {
    	//заполнение ListView
    	swipeLayout.setRefreshing(false);
    	if (lvDash.getHeaderViewsCount() == 0) {
    		lvDash.addHeaderView(llHeader);
    	}
    	if ((menuActivity.dataDash.size() > 24) & (lvDash.getFooterViewsCount() == 0)) {
    		lvDash.addFooterView(llFooter);
    	}
    	if ((menuActivity.dataDash.size() < 25) & (menuActivity.currentPage == 1)) {
    		lvDash.removeFooterView(llFooter);
    	}
    	sAdapter = new UserEntryAdapter(context, menuActivity.dataDash);
    	lvDash.setAdapter(sAdapter);
    	        
    	lvDash.setOnItemClickListener(new OnItemClickListener() {
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
    	        
    	lvDash.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        if ((lastVisibleItemPosition - 1) == (menuActivity.dataDash.size())) {
        	((MenuActivity) context).currentPage += 1;
        	tvFooterText.setText(menuActivity.getString(R.string.loading));
        	dashSupport.getData(menuActivity.currentPage, 
        			menuActivity.token, menuActivity.nativeCurrency);
        }
    }
	
    public void removeFooter() {
		lvDash.removeFooterView(llFooter);
	}
    
    public void setViewPager() {
    	srgDash.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    		@Override
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			switch (checkedId) {
    			case R.id.rbHour:
    				vpDash.setCurrentItem(0, true);
    				createPlot(menuActivity.dataPlotDayX, menuActivity.dataPlotDay);
    				menuActivity.currentPlot = menuActivity.PLOT_24;
    				setPercent(menuActivity.percentDay);
        			break;
    			case R.id.rbWeek:
    				vpDash.setCurrentItem(1);
    				createPlot(menuActivity.dataPlotWeekX, menuActivity.dataPlotWeek);
    				menuActivity.currentPlot = menuActivity.PLOT_WEEK;
    				setPercent(menuActivity.percentWeek);
    				break;
    			case R.id.rbMonth:
    				vpDash.setCurrentItem(2);
    				createPlot(menuActivity.dataPlotMonthX, menuActivity.dataPlotMonth);
    				menuActivity.currentPlot = menuActivity.PLOT_30;
    				setPercent(menuActivity.percentMonth);
    				break;
    			default:
    				break;
    			}
    		}
    	});
        		
    	    vpDash = (ViewPager) llHeader.findViewById(R.id.vpDash);
    	    pagerAdapter = new MainVPAdapter(context, 
    	    		menuActivity.dataDayWeekMonth, menuActivity.dataDWMCurrency);
		    vpDash.setAdapter(pagerAdapter);
		    vpDash.setOnPageChangeListener(new OnPageChangeListener() {
		          @Override
		          public void onPageSelected(int position) {
		            //Log.d("1", "onPageSelected, position = " + position);
		            if (position == 0) {
		            	rbHour.setChecked(true);
		            } else if (position == 1) {
		            	rbWeek.setChecked(true);
		            } else {
		            	rbMonth.setChecked(true);
		            }
		          }
		
		          @Override
		          public void onPageScrolled(int position, float positionOffset,
		              int positionOffsetPixels) {
		          }
		
		          @Override
		          public void onPageScrollStateChanged(int state) {
		          }
	        });
	}
    
    public void createPlot(ArrayList<String> dataPlotX, ArrayList<Integer> dataPlotY) {
    	mChart.setOnChartGestureListener(new OnChartGestureListener() {
			
			@Override
			public void onChartSingleTapped(MotionEvent me) {
			}
			
			@Override
			public void onChartLongPressed(MotionEvent me) {
			}
			
			@Override
			public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
					float velocityY) {
				//Log.d("1", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
				int currVP = vpDash.getCurrentItem();
				if (velocityX < 0) {
					Log.d("1", "Свайп влево");
					if (!(currVP == 2)) {
						vpDash.setCurrentItem(currVP + 1);
					}
				} else {
					Log.d("1", "Свайп вправо");
					if (!(currVP == 0)) {
						vpDash.setCurrentItem(currVP - 1);
					}
				}
			}
			
			@Override
			public void onChartDoubleTapped(MotionEvent me) {
			}
		});
    	
        //mChart.setUnit(" $");
        mChart.setDrawUnitsInChart(false);

        // if enabled, the chart will always start at zero on the y-axis
        mChart.setStartAtZero(false);

        // disable the drawing of values into the chart
        mChart.setDrawYValues(false);

        mChart.setDrawBorder(false);
//        mChart.setBorderPositions(new BorderPosition[] {
//                BorderPosition.BOTTOM
//        });

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("");

        // enable value highlighting
        mChart.setHighlightEnabled(true);
       
        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawVerticalGrid(false);
        mChart.setDrawHorizontalGrid(false);
        mChart.setDrawLegend(false);
        mChart.setPadding(0, 0, 0, 0);
        //mChart.setFadingEdgeLength(20);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.BLACK);
        
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // TO use for it
        MyMarkerView mv = new MyMarkerView(menuActivity, R.layout.custom_marker_view);

        // set the marker TO the chart
        mChart.setMarkerView(mv);

        // add data
        //ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < dataPlotY.size(); i++) {
        	//xVals.add(i + "");
        	yVals.add(new Entry(dataPlotY.get(i), i));
        }
        
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setColor(Color.parseColor("#ADFF2F"));
        //set1.setCircleColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(2f);
        //set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(117, 117, 117));
        set1.setDrawCircles(false);
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.05f);
        

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataPlotX, dataSets);

        // set data
        mChart.setData(data);

        mChart.animateXY(2000, 2000);

        //get the legend (only possible after setting data)
        //Legend l = mChart.getLegend();
        //l.setTextColor(Color.TRANSPARENT);
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
//        l.setForm(LegendForm.LINE);
//        l.setTextColor(Color.WHITE);
//
//        XLabels xl = mChart.getXLabels();
//        xl.setTextColor(Color.WHITE);
//
//        YLabels yl = mChart.getYLabels();
//        yl.setTextColor(Color.WHITE);
            	
    }
    
    public void setHeaderText(String text) {
    	tvFooterText.setText(text);
    }
    
    public void setPercent(String text) {
    	if (text.isEmpty()) {
    		tvPercent.setText("");
    		ivPercent.setVisibility(View.INVISIBLE);
    	} else if (text.startsWith("-")) {
    		tvPercent.setText(text.replace("-", ""));
    		tvPercent.setTextColor(Color.RED);
    		ivPercent.setVisibility(View.VISIBLE);
    		ivPercent.setImageResource(R.drawable.down);
    	} else {
    		tvPercent.setText(text);
    		tvPercent.setTextColor(Color.parseColor("#9ACD32"));
    		ivPercent.setVisibility(View.VISIBLE);
    		ivPercent.setImageResource(R.drawable.up);
    	}
    }
}
