/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.w1.merchant.android.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.extra.DashSupport;
import com.w1.merchant.android.extra.DialogExit;
import com.w1.merchant.android.extra.InvoiceSupport;
import com.w1.merchant.android.extra.UserEntrySupport;
import com.w1.merchant.android.extra.ViewPagerAdapter;
import com.w1.merchant.android.request.GETBalance;
import com.w1.merchant.android.request.GETProfile;
import com.w1.merchant.android.request.GETTemplateList;
import com.w1.merchant.android.request.HttpDELETE;
import com.w1.merchant.android.request.JSONParsing;
import com.w1.merchant.android.request.Urls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MenuActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private static MenuActivity mContext;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] menuItems;
    
    private static final int ACT_ADD = 1;
    
    private static final int FRAGMENT_USERENTRY = 1;
    private static final int FRAGMENT_INVOICE = 2;
    public static final int FRAGMENT_DASH = 3;
    
    public final int PLOT_24 = 1;
    public final int PLOT_WEEK = 2;
    public final int PLOT_30 = 3;
    
    public static final String ATTRIBUTE_NAME_NUMBER = "number";
    public static final String ATTRIBUTE_NAME_DATE = "date";
    public static final String ATTRIBUTE_NAME_IMAGE = "image";
    public static final String ATTRIBUTE_NAME_AMOUNT = "amount";
    public static final String ATTRIBUTE_NAME_DESCR = "descr";
    public static final String ATTRIBUTE_NAME_STATE = "state";
    public static final String ATTRIBUTE_NAME_RUBL = "rubl";
	
	String[] from = { ATTRIBUTE_NAME_NUMBER, ATTRIBUTE_NAME_DATE,
			ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_AMOUNT,
			ATTRIBUTE_NAME_DESCR, ATTRIBUTE_NAME_STATE,
			ATTRIBUTE_NAME_RUBL};
	int[] to = { R.id.tvNumber, R.id.tvDate, R.id.ivIcon,
			R.id.tvAmount, R.id.tvDescr, R.id.tvState, R.id.tvRubl };
    
    ImageView ivAccountIcon;
    Intent intent;
    Activity activity;
    HttpDELETE httpDELETE;
    String[] requestData = { "", "", "", "" };
    String[] requestData2 = { "", "", "", "" };
    String[] httpResult = { "", "" };
    String[] result = { "", "", "" };
    GETBalance getBalance;
    GETProfile getProfile;
    GETTemplateList getTemplateList;
    String httpResultStr, name, logo, logoUrl, balance;
    int	sumInc = 0;
    int comisInc = 0;
    int	sumOut = 0;
    int comisOut = 0;
    public String waitSum = "";
    public String percentDay = "";
    public String percentWeek = ""; 
    public String percentMonth = "";
    public String filter = "";
    public String nativeCurrency = "";
    public ArrayList<Map<String, Object>> dataUserEntry, 
    	dataDash, dataInvoice;
    public ArrayList<Integer> dataPlotDay, dataPlotWeek, dataPlotMonth;
    public ArrayList<String> dataPlotDayX, dataPlotWeekX, 
    		dataPlotMonthX, dataDayWeekMonth, dataDWMCurrency;
    public ArrayList<String[]> dataTemplate, dataPeriod, dataBalance;
    UserEntrySupport userEntrySupport;
    DashSupport dashSupport;
    InvoiceSupport invoiceSupport;
    int currentPage = 0;
    int currentFragment = 0;
    int currPageUEGraph = 0;
    int currPageUETotal = 0;
    public int currentPlot = 0;
    UserEntryFragment fragmentUserEntry;
	DashFragment fragmentDash;
	InvoiceFragment fragmentInvoice;
	TemplateFragment fragmentTemplate;
    LinearLayout llHeader;
    TextView tvBack, tvDate, tvNext, tvName, tvUrl, tvABName, tvABRub;
	int current = 0;
	int totalReq = 0;
	int day0, month0, year0, day1, month1, year1;
	DatePicker dp1;
	SearchView svList;
	public String token, userId, timeout;
	ProgressBar progressBar;
	private SearchView mSearchView;
	DialogFragment dlgExit;
	public boolean accountTypeId = false;
	ViewPager vpCurrency;
	PagerAdapter currencyPagerAdapter;
	ArrayList<String> currSumNames;
	ArrayList<String> currRubls;
	ArrayList<String> currCodes;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_PROGRESS);
        //this.setProgressBarIndeterminate(true);
        setContentView(R.layout.activity_main);
        mContext = this;
        
        intent = getIntent();
        token = intent.getStringExtra("token");
        userId = intent.getStringExtra("userId");
        timeout = intent.getStringExtra("timeout");
        
        Timer myTimer;
    	myTimer = new Timer();
    	myTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Intent intent = new Intent();
			    setResult(RESULT_OK, intent);
				exit();
			}
		}, Integer.parseInt(timeout) * 1000);
        
        dlgExit = new DialogExit();
        
        // create new ProgressBar and style it
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
        progressBar.setIndeterminate(false);

        // retrieve the top view of our application
        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(progressBar);

        // Here we try to position the ProgressBar to the correct position by looking
        // at the position where content area starts. But during creating time, sizes 
        // of the components are not set yet, so we have to wait until the components
        // has been laid out
        // Also note that doing progressBar.setY(136) will not work, because of different
        // screen densities and different sizes of actionBar
        ViewTreeObserver observer = progressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                progressBar.setY(contentView.getY() - 10);

                ViewTreeObserver observer = progressBar.getViewTreeObserver();
                observer.removeGlobalOnLayoutListener(this);
            }
        });
        
        userEntrySupport = new UserEntrySupport(mContext);
        dashSupport = new DashSupport(mContext);
        invoiceSupport = new InvoiceSupport(mContext);
        
        fragmentUserEntry = new UserEntryFragment();
        fragmentDash = new DashFragment();
        fragmentInvoice = new InvoiceFragment();
        fragmentTemplate = new TemplateFragment();
        
        dataPlotDay = new ArrayList<Integer>();
        dataPlotWeek = new ArrayList<Integer>();
        dataPlotMonth = new ArrayList<Integer>();
        dataPlotDayX = new ArrayList<String>();
        dataPlotWeekX = new ArrayList<String>();
        dataPlotMonthX = new ArrayList<String>();
        dataTemplate = new ArrayList<String[]>();
        dataPeriod = new ArrayList<String[]>();
        dataBalance = new ArrayList<String[]>();
        dataDayWeekMonth = new ArrayList<String>();
        dataDWMCurrency = new ArrayList<String>();
        currSumNames = new ArrayList<String>();
    	currRubls = new ArrayList<String>();
    	currCodes = new ArrayList<String>();
        
        getProfile();
//        clearDataArrays();
//        getBalance();
        
        mTitle = mDrawerTitle = getTitle();
        menuItems = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        
        //шапка меню
        LayoutInflater inflater = getLayoutInflater();
        llHeader = (LinearLayout) inflater.inflate(R.layout.header_menu, null);
        mDrawerList.addHeaderView(llHeader);
        ivAccountIcon = (ImageView) findViewById(R.id.ivAccountIcon);
        tvName = (TextView) findViewById(R.id.tvName);
        tvUrl = (TextView) findViewById(R.id.tvUrl);
        final String ATTRIBUTE_NAME_TEXT = "text";
        final String ATTRIBUTE_NAME_IMAGE = "image";
        int[] img = { R.drawable.menu_dashboard, R.drawable.menu_account,
        		R.drawable.menu_check, R.drawable.menu_output, R.drawable.menu_settings};
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(5);
        Map<String, Object> m;
        for (int i = 0; i < 5; i++) {
	          m = new HashMap<String, Object>();
	          m.put(ATTRIBUTE_NAME_TEXT, getResources().getStringArray(R.array.menu_array)[i]);
	          m.put(ATTRIBUTE_NAME_IMAGE, img[i]);
	          data.add(m);
        }
        String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE };
        int[] to = { R.id.tvText, R.id.ivImg };
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.menu_item,
            from, to);
        
        mDrawerList.setAdapter(sAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setCustomView(R.layout.action_bar_rubl2);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM |
        		ActionBar.DISPLAY_SHOW_HOME |
        		ActionBar.DISPLAY_HOME_AS_UP);
        vpCurrency = (ViewPager) getActionBar().getCustomView().
        		findViewById(R.id.vpCurrency);
        vpCurrency.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				//меняем валюту
				nativeCurrency = currCodes.get(arg0);
				currentPage = 1;
				clearDataArrays();
				//запрос списка и данных для графика
		       	dashSupport.getData(currentPage, token, nativeCurrency);
		       	currPageUEGraph = 1;
			    dashSupport.getDataPeriod(getDate60DaysAgo(), token,
			    		nativeCurrency, currPageUEGraph + "");
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
//                tvABName.setText(mTitle);
//                if (currentFragment == FRAGMENT_DASH) {
//                	tvABRub.setText("B");
//                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
//                tvABName.setText(mDrawerTitle);
//                tvABRub.setText("");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
    }
    
    public static Context getContext(){
        return mContext;
    }
    
    //запрос баланса
    public void getBalance() {
    	requestData2[0] = Constants.URL_BALANCE;
        requestData2[1] = token;
        requestData2[2] = "";
		getBalance = new GETBalance(mContext);
		getBalance.execute(requestData2);
    }
    
    //ответ на запрос баланса
    public void setBalance(ArrayList<String[]> result) {
    	currSumNames.clear();
    	currRubls.clear();
    	currCodes.clear();
    	dataBalance = result;
    	waitSum = "";
    	for (int i = 0; i < result.size(); i++) {
    		String[] line = { "", "", "", "", "" };
    		line = result.get(i);
    			if (line[1].equals("RUB")) {
	    			currSumNames.add(getString(R.string.balance) + " " + line[2]);
	    			currRubls.add("B");
    			} else {
    				if (!line[2].equals("0")) {
	    				currSumNames.add(getString(R.string.balance) + " " +
	    						line[2] + " " + line[1]);
		    			currRubls.add("");
    				}
    			}
    			currCodes.add(line[0]);
			if (!line[3].equals("0")) {
    			waitSum += line[3] + " " + line[1] + ", ";
    		}
    		if (line[4].equals("true")) {
    			if (nativeCurrency.isEmpty()) {
    				nativeCurrency = line[0];
    			}
    		}
		}
    	if (waitSum.endsWith(", ")) {
    		waitSum = waitSum.substring(0, waitSum.length() - 2);
    	}
    	currencyPagerAdapter = new ViewPagerAdapter(this, 
    			currSumNames, currRubls);
    	vpCurrency.setAdapter(currencyPagerAdapter);
    	//запрос списка и данных для графика
       	dashSupport.getData(currentPage, token, nativeCurrency);
       	currPageUEGraph = 1;
	    dashSupport.getDataPeriod(getDate60DaysAgo(), token,
	    		nativeCurrency, currPageUEGraph + "");
    }
    
    public void getProfile() {
    	//Получение профиля пользователя (название, логотип, url)
    	requestData[0] = Constants.URL_PROFILE + userId;
    	requestData[1] = token;
    	requestData[2] = "";
    	getProfile = new GETProfile(mContext);
    	getProfile.execute(requestData);	
    }

    //ответ на запрос профиля 
    public void setProfile(String[] result) {
    	if (!TextUtils.isEmpty(result[2])) {
    		Picasso.with(this)
    		.load(result[2])
    		.into(ivAccountIcon);		
    	} else {
    		Picasso.with(this)
    		.load(R.drawable.no_avatar)
    		.into(ivAccountIcon);
    	}
    	tvName.setText(result[0]);
    	tvUrl.setText(result[1]);
    	accountTypeId = result[3].equals("Business");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.ic_menu_add).setVisible(!drawerOpen);
        if ((currentFragment == 0) | (currentFragment == FRAGMENT_DASH)) {
        	menu.findItem(R.id.ic_menu_add).setVisible(false);
        	menu.findItem(R.id.ic_menu_search).setVisible(false);
		}
        mSearchView = (SearchView) menu.findItem(
				R.id.ic_menu_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				//поиск в списках
				if (!TextUtils.isEmpty(newText)) {
					currentPage = 1;
					if (currentFragment == FRAGMENT_INVOICE) {
						invoiceSupport.getData(filter, currentPage, newText, token);
					}  else if (currentFragment == FRAGMENT_USERENTRY) {
						userEntrySupport.getData(filter, currentPage,
								newText, token, nativeCurrency);
					}
				}
				return true;
			}
		});
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.ic_menu_add:
        	if (currentFragment == FRAGMENT_INVOICE) {
				//добавления счета
				intent = new Intent(mContext, AddInvoice.class);
				intent.putExtra("token", token);
				startActivityForResult(intent, ACT_ADD);
			} else if (currentFragment == FRAGMENT_USERENTRY) {
				//итоги по выписке
				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				
				Calendar today = Calendar.getInstance();
		        day1 = today.get(Calendar.DAY_OF_MONTH);
		        month1 = today.get(Calendar.MONTH);
		        year1 = today.get(Calendar.YEAR);
		        
		        today.add(Calendar.MONTH, -1);
		        day0 = today.get(Calendar.DAY_OF_MONTH);
		        month0 = today.get(Calendar.MONTH);
		        year0 = today.get(Calendar.YEAR);
		        
				@SuppressLint("InflateParams") View popupView = layoutInflater.inflate(R.layout.popup_date, null);
				final PopupWindow popupWindow = new PopupWindow(popupView,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				dp1 = (DatePicker) popupView.findViewById(R.id.dp1);
				dp1.init(year0, month0, day0, null);
				
				tvBack = (TextView) popupView.findViewById(R.id.tvBack);
				tvDate = (TextView) popupView.findViewById(R.id.tvDate);
				tvNext = (TextView) popupView.findViewById(R.id.tvNext);
				
		        tvNext.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (current == 0) {
							tvBack.setText(getString(R.string.go_back));
							tvDate.setText(getString(R.string.end_date));
							current += 1;
							day0 = dp1.getDayOfMonth();
					        month0 = dp1.getMonth() + 1;
					        year0 = dp1.getYear();
					        dp1.init(year1, month1, day1, null);
						} else {
							//запускаем итоги по выписке
							current = 0;
							popupWindow.dismiss();
							day1 = dp1.getDayOfMonth();
					        month1 = dp1.getMonth() + 1;
					        year1 = dp1.getYear();
					        sumInc = 0;
					        comisInc = 0;
					        sumOut = 0;
					        comisOut = 0;
					        currPageUETotal = 1;
							dashSupport.getDataTotal("" + year0 + "-" + month0 + "-" + day0,
									"" + year1 + "-" + month1 + "-" + day1, "Inc", token, 
									nativeCurrency, currPageUETotal + "");
						}
					}
				});
				
		        tvBack.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (current == 0) {
							//выходим
							current = 0;
							popupWindow.dismiss();
						} else {
							tvBack.setText(getString(R.string.cancel));
							tvDate.setText(getString(R.string.begin_date));
							dp1.init(year0, month0, day0, null);
							current -= 1;
						}
					}
				});
				popupWindow.showAsDropDown(progressBar, 0, 0);
			}
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	if (position > 0) {
        		selectItem(position);
        	}
        }
    }

    public void selectItem(int position) {
    	currentPage = 1;
    	//currencyPagerAdapter.notifyDataSetChanged();
        if (position == 1) {
        	clearDataArrays();
	       	getBalance();
	       	currentFragment = FRAGMENT_DASH;
	       	changeFragment(fragmentDash);
        } else if (position == 2) {
        	currentFragment = FRAGMENT_USERENTRY;
        	userEntrySupport.getData("", currentPage, "", 
        			token, nativeCurrency);
        	changeFragment(fragmentUserEntry);
        } else if (position == 3) {
        	currentFragment = FRAGMENT_INVOICE;
        	invoiceSupport.getData("", currentPage, "",	token);
            changeFragment(fragmentInvoice);
        } else if (position == 4) {
        	//запрос шаблонов
            requestData[0] = Constants.URL_TEMPLATES;
            requestData[1] = token;
            requestData[2] = "";
            getTemplateList = new GETTemplateList(mContext);
            getTemplateList.execute(requestData);
            currentFragment = 0;
            changeFragment(fragmentTemplate);
        } else if (position == 5) {
        	dlgExit.show(getFragmentManager(), "dlgExit");
        }
        
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        if (position == 1) {
        	if (!currSumNames.isEmpty()) {
	        	currencyPagerAdapter = new ViewPagerAdapter(this, 
	        			currSumNames, currRubls);
	        	vpCurrency.setAdapter(currencyPagerAdapter);
        	}
        } else {
        	ArrayList<String> abName = new ArrayList<String>();
        	ArrayList<String> abRubl = new ArrayList<String>();
        	abName.add(menuItems[position - 1]);
        	abRubl.add("");
        	currencyPagerAdapter = new ViewPagerAdapter(this, 
        			abName, abRubl);
        	vpCurrency.setAdapter(currencyPagerAdapter);
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private void clearDataArrays() {
    	dataPlotDay.clear();
       	dataPlotWeek.clear();
       	dataPlotMonth.clear();
       	dataPlotDayX.clear();
       	dataPlotWeekX.clear();
       	dataPlotMonthX.clear();
       	dataPeriod.clear();
    }
    
    private void changeFragment(Fragment targetFragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
    
    public void setActionBarText(CharSequence title, String rubl) {
//    	mTitle = title;
//        tvABName.setText(mTitle);
//        tvABRub.setText(rubl);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //ответ список шаблонов
    public void addTemplateList(ArrayList<String[]> newData) {
    	dataTemplate = newData;
    	fragmentTemplate.setAdapter();
	}
    
    //ответ итоги по выписке Inc
    public void userEntryInc(String[] result) {
    	int[] data = { 0, 0 };
    	data = JSONParsing.userEntryTotal(result[1]);
    	if ((data[0] > 0) | (data[1] > 0)) {
	    	sumInc += data[0];
	    	comisInc += data[1];
	    	currPageUETotal += 1;
			dashSupport.getDataTotal("" + year0 + "-" + month0 + "-" + day0,
					"" + year1 + "-" + month1 + "-" + day1, "Inc", token, 
					nativeCurrency, currPageUETotal + "");
    	} else {
	    	//итоги по выписке Out
    		currPageUETotal = 1;
	    	dashSupport.getDataTotal("" + year0 + "-" + month0 + "-" + day0,
					"" + year1 + "-" + month1 + "-" + day1, "Out", token, 
					nativeCurrency, currPageUETotal + "");
    	}
    }
    
    //ответ итоги по выписке Out
    public void userEntryOut(String[] result) {
    	int[] data = { 0, 0 };
    	data = JSONParsing.userEntryTotal(result[1]);
    	if ((data[0] > 0) | (data[1] > 0)) {
	    	sumOut += data[0];
	    	comisOut += data[1];
	    	currPageUETotal += 1;
			dashSupport.getDataTotal("" + year0 + "-" + month0 + "-" + day0,
					"" + year1 + "-" + month1 + "-" + day1, "Out", token, 
					nativeCurrency, currPageUETotal + "");
    	} else {
    		intent = new Intent(mContext, UserEntryTotal.class);
	    	intent.putExtra("SumIn", sumInc);
	    	intent.putExtra("ComisIn", comisInc);
	    	intent.putExtra("SumOut", sumOut);
	    	intent.putExtra("ComisOut", comisOut);
	    	intent.putExtra("nativeCurrency", nativeCurrency);
			startActivity(intent);
    	}
    }

    //подтверждение добавления счета
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == ACT_ADD) {
			if (resultCode == RESULT_OK) {
				Intent confirmIntent = new Intent(getApplicationContext(), 
						ConfirmActivity.class);
				startActivity(confirmIntent);
			}
		}
	}
    
    @Override
	public void onBackPressed() {
    	//dlgExit.show(getFragmentManager(), "dlgExit");
    	mDrawerLayout.closeDrawer(mDrawerList);
    	if (!(currentFragment == FRAGMENT_DASH)) {
    		selectItem(1);
    		invalidateOptionsMenu();
    	}
    }
    
    public void exit() {
    	closeSession();
		finish();
    }
    
    //закрытие сессии
    void closeSession() {
    	requestData[0] = Urls.URL + Urls.URL_CLOSE_SESSION;
    	requestData[1] = token;
    	httpDELETE = new HttpDELETE(mContext);
		httpDELETE.execute(requestData);
	}
    
  //ответ выписка
    public void addUserEntry(String data) {
    	ArrayList<Map<String, Object>> newData = JSONParsing.userEntry(data, userId);
    	if (!(newData == null)) {
	    	if (currentPage == 1) {
	    		dataUserEntry = newData;
	    		fragmentUserEntry.createListView();
	    	} else {
				for (int j = 0; j < newData.size(); j++) {
					dataUserEntry.add(newData.get(j));
		    		fragmentUserEntry
		    			.sAdapter.notifyDataSetChanged();
		    	}
				fragmentUserEntry
				.setHeaderText(getString(R.string.data_load));
	    	}
    	} else {
    		fragmentUserEntry.removeFooter();
    	}
    }
    
    //ответ дэш 
    public void addDash(String data) {
    	ArrayList<Map<String, Object>> newData = JSONParsing.userEntry(data, userId);
    	if (!(newData == null)) {
    		if (!(newData.size() == 0)) {
		    	if (currentPage == 1) {
		    		dataDash = newData;
		    		fragmentDash.createListView();
		    	} else {
					for (int j = 0; j < newData.size(); j++) {
						dataDash.add(newData.get(j));
			    		fragmentDash.sAdapter.notifyDataSetChanged();
			    	}
					fragmentDash
					.setHeaderText(getString(R.string.data_load));
		    	}
    		} else {
        		fragmentDash.removeFooter();
        	}
    	} else {
    		fragmentDash.removeFooter();
    	}
	}
    
    //ответ счета
    public void addInvoice(ArrayList<Map<String, Object>> newData) {
    	if (!(newData.size() == 0)) {
	    	if (currentPage == 1) {
	    		dataInvoice = newData;
	    		fragmentInvoice.createListView();
	    	} else {
				for (int j = 0; j < newData.size(); j++) {
					dataInvoice.add(newData.get(j));
		    		fragmentInvoice.sAdapter
		    			.notifyDataSetChanged();
		    	}
				fragmentInvoice
				.setHeaderText(getString(R.string.data_load));
	    	}
    	} else {
    		if (currentPage == 1) {
    			if (!(dataInvoice == null)) {
    			dataInvoice.clear();
	    			fragmentInvoice.sAdapter
	    				.notifyDataSetChanged();
    			}
    		}
    		fragmentInvoice.removeFooter();
    	}
	}
    
    //получение данных для графика, для ViewPager, для процентов
    public void setDataGraphVPPercents(String[] result) {
    	ArrayList<String[]> newDataPeriod = JSONParsing.userEntryPeriod(result[1]);
    	if (!newDataPeriod.isEmpty()) {
	    	for (int i = 0; i < newDataPeriod.size(); i++) {
				dataPeriod.add(newDataPeriod.get(i));
			}
	    	currPageUEGraph += 1;
		    dashSupport.getDataPeriod(getDate60DaysAgo(), token,
		    		nativeCurrency, currPageUEGraph + "");
    	} else {
    		dataGraphVPPercents();
    	}
    }
    
    //подготовка данных для графика, для ViewPager, для процентов
    public void dataGraphVPPercents() {
    	String[] dataPeriodElem = { "", "", "" };
    	Date currentDate = new Date();
    	float fSumDay = 0;
    	float fSumWeek = 0;
    	float fSumMonth = 0;
    	float fSumDay2 = 0;
    	float fSumWeek2 = 0;
    	float fSumMonth2 = 0;
    	int sum = 0;
    	String year, month, day, hour, minute, second, in;
    	long diffSecond = 0; 
		int diffHour = 0;
		int diffDay = 0;
		Calendar calendar;
		Calendar currDate;
    	
    	currDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
//    	currDate.set(Calendar.HOUR_OF_DAY, 20);
//    	currDate.set(Calendar.MINUTE, 59);
//    	currDate.set(Calendar.SECOND, 59);
//    	currDate.set(Calendar.MILLISECOND, 999);
    	
    	for (int i = 23; i > -1; i--) {
    		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
    		calendar.add(Calendar.HOUR_OF_DAY, -i);
    		dataPlotDay.add(0);
    		dataPlotDayX.add(calendar.get(Calendar.HOUR_OF_DAY) + " " +
    				getString(R.string.hour_cut));
    	}
    	for (int i = 6; i > -1; i--) {
    		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
    		calendar.add(Calendar.DAY_OF_YEAR, -i);
        	dataPlotWeek.add(0);
        	dataPlotWeekX.add(getResources().getStringArray(R.array.day_of_week)
        			[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
    	}
    	for (int i = 29; i > -1; i--) {
    		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+3"));
    		calendar.add(Calendar.DAY_OF_YEAR, -i);
    		dataPlotMonth.add(0);
    		dataPlotMonthX.add(calendar.get(Calendar.DAY_OF_MONTH) 
    				+ " " + getResources().getStringArray(R.array.month_array_cut)
    				[calendar.get(Calendar.MONTH)] +
    				", " + getResources().getStringArray(R.array.day_of_week)
        			[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
    	}
    	
    	//за 24 часа и пред день
    	for (int i = 0; i < dataPeriod.size(); i++) {
    		dataPeriodElem = dataPeriod.get(i);
    		//JSONParsing.appendLog(dataPeriodElem[1] + " " + dataPeriodElem[0]);
    		in = dataPeriodElem[1];
    		
    		year = in.substring(0, 4);
    		month = in.substring(5, 7);
    		day = in.substring(8, 10);
    		hour = in.substring(11, 13);
    		minute = in.substring(14, 16);
    		second = in.substring(17, 19);
    		
    		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    		calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 
    				Integer.parseInt(day), Integer.parseInt(hour),
    				Integer.parseInt(minute), Integer.parseInt(second));
    		diffSecond = (currentDate.getTime() - calendar.getTime().getTime()) / 1000;
    		diffHour = (int) diffSecond / 3600; 
    		
    		sum = Math.round((Float.parseFloat(dataPeriodElem[0]) - 
    				Float.parseFloat(dataPeriodElem[2])));
    		
    		if (diffHour < 24) {
    			fSumDay += sum;
    			dataPlotDay.set(23 - diffHour, dataPlotDay.get(23 - diffHour) + sum);
    		//предыдущий день для процента
    		} else if (diffHour < 48) {
    			fSumDay2 += sum;
    		}
		}
    	
    	// за неделю, месяц и пред
    	for (int i = 0; i < dataPeriod.size(); i++) {
    		dataPeriodElem = dataPeriod.get(i);
    		in = dataPeriodElem[1];
    		
    		year = in.substring(0, 4);
    		month = in.substring(5, 7);
    		day = in.substring(8, 10);
    		hour = in.substring(11, 13);
    		minute = in.substring(14, 16);
    		second = in.substring(17, 19);
    		
    		calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    		calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 
    				Integer.parseInt(day), Integer.parseInt(hour),
    				Integer.parseInt(minute), Integer.parseInt(second));
    		
    		sum = Math.round((Float.parseFloat(dataPeriodElem[0]) - 
    				Float.parseFloat(dataPeriodElem[2])));
    		
    		diffDay = (int) ((currDate.getTime().getTime() - 
    				calendar.getTime().getTime()) / 86400000);
    		
    		if (diffDay < 7) {
    			dataPlotWeek.set(6 - diffDay, dataPlotWeek.get(6 - diffDay) + sum);
    			dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
    			fSumWeek += sum;
			} else if (diffDay < 14) {
				fSumWeek2 += sum;
				dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
			} else if (diffDay < 30){
				dataPlotMonth.set(29 - diffDay, dataPlotMonth.get(29 - diffDay) + sum);
				fSumMonth += sum;
			} else {
				fSumMonth2 += sum;
			}
		}
    	
    	fSumMonth += fSumWeek2;
    	fSumMonth += fSumWeek;
    	//Log.d("1", fSumDay + " " + fSumWeek + " " + fSumMonth);
    	
    	clearVPData();
    	dataDayWeekMonth.add(JSONParsing.formatNumberNoFract(fSumDay + ""));
    	dataDWMCurrency.add(JSONParsing.getCurrencySymbol(nativeCurrency));
    	dataDayWeekMonth.add(JSONParsing.formatNumberNoFract(fSumWeek + ""));
    	dataDWMCurrency.add(JSONParsing.getCurrencySymbol(nativeCurrency));
    	dataDayWeekMonth.add(JSONParsing.formatNumberNoFract(fSumMonth + ""));
    	dataDWMCurrency.add(JSONParsing.getCurrencySymbol(nativeCurrency));
    	
    	if (fSumDay2 > 0) {
    		percentDay = (int) (fSumDay / (fSumDay2 / 100) - 100) + " %";
    	} else {
    		percentDay = "";
    	}
    	
    	if (fSumWeek2 > 0) {
    		percentWeek = (int) (fSumWeek / (fSumWeek2 / 100) - 100) + " %";
    	} else {
    		percentWeek = "";
    	}
    	
    	if (fSumMonth2 > 0) {
    		percentMonth = (int) (fSumMonth / (fSumMonth2 / 100) - 100) + " %";
    	} else {
    		percentMonth = "";
    	}
    	
    	fragmentDash.setViewPager();
    	fragmentDash.createPlot(dataPlotDayX, dataPlotDay);
    	fragmentDash.setPercent(percentDay);
    	currentPlot = PLOT_24;
	}
    
    void clearVPData() {
    	dataDayWeekMonth.clear();
    	dataDWMCurrency.clear();
    }
    
    public String getDate60DaysAgo() {
    	String result = "";
    	String month = "";
    	String day = "";
    	Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    	calendar.add(Calendar.DAY_OF_MONTH, -60);
    	month = calendar.get(Calendar.MONTH) + 1 + "";
    	if (month.length() == 1) {
    		month = "0" + month;
		}
    	day = calendar.get(Calendar.DAY_OF_MONTH) + "";
    	if (day.length() == 1) {
    		day = "0" + day;
		}
    	result = calendar.get(Calendar.YEAR) + "-" + month + "-" + day;
		return result;
    }
    
    public void startPBAnim() {
    	totalReq += 1;
    	if (totalReq == 1) {
    		progressBar.setIndeterminate(true);
    	}
    }
    
    public void stopPBAnim() {
    	totalReq -= 1;
    	if (totalReq == 0) {
    		progressBar.setIndeterminate(false);
    	}
    }
    
    public String getWaitSum() {
    	return waitSum;
    }
    
    public int getCurrentFragment() {
    	return currentFragment;
    }
}