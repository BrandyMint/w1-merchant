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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.DialogExit;
import com.w1.merchant.android.extra.ViewPagerAdapter;
import com.w1.merchant.android.model.Balance;
import com.w1.merchant.android.model.Profile;
import com.w1.merchant.android.service.ApiProfile;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiSessions;
import com.w1.merchant.android.support.TicketListActivity;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;
import com.w1.merchant.android.viewextended.CircleTransformation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MenuActivity extends FragmentActivity implements UserEntryFragment.OnFragmentInteractionListener,
    InvoiceFragment.OnFragmentInteractionListener,
    DashFragment.OnFragmentInteractionListener,
        TemplateFragment.OnFragmentInteractionListener
{
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager vpCurrency;

    private static final int FRAGMENT_USERENTRY = 1;
    private static final int FRAGMENT_INVOICE = 2;
    public static final int FRAGMENT_DASH = 3;

    private ImageView ivAccountIcon;

    private List<Balance> mBalances = new ArrayList<>();
    public String nativeCurrency = "643";

    private int currentFragment = 0;

    private DashFragment fragmentDash;

    private TextView tvName;
    private TextView tvUrl;
    private int totalReq = 0;
    private ProgressBar progressBar;
    private boolean mIsBusinessAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Session.getInstance().hasToken()) {
            Utils.restartApp(this);
            return;
        }

        // TODO избавиться
        long timeout = Session.getInstance().getAuthTimeout() * 1000;
        Timer myTimer;
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                exit();
            }
        }, timeout);

        // create new ProgressBar and style it
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
        progressBar.setIndeterminate(false);

        // retrieve the top view of our application
        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(progressBar);

        // Here we try TO position the ProgressBar TO the correct position by looking
        // at the position where content area starts. But during creating time, sizes 
        // of the components are not set yet, so we have TO wait until the components
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

        fragmentDash = new DashFragment();

        loadProfile();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        //шапка меню
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout llHeader = (LinearLayout) inflater.inflate(R.layout.header_menu, null);
        mDrawerList.addHeaderView(llHeader);
        ivAccountIcon = (ImageView) findViewById(R.id.ivAccountIcon);
        tvName = (TextView) findViewById(R.id.tvName);
        tvUrl = (TextView) findViewById(R.id.tvUrl);
        final String ATTRIBUTE_NAME_TEXT = "text";
        final String ATTRIBUTE_NAME_IMAGE = "image";
        int[] img = {R.drawable.menu_dashboard, R.drawable.menu_account,
                R.drawable.menu_check, R.drawable.menu_output,
                R.drawable.menu_support,
                R.drawable.menu_settings
        };
        ArrayList<Map<String, Object>> data = new ArrayList<>(6);
        Map<String, Object> m;
        for (int i = 0; i < 6; i++) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_TEXT, getResources().getStringArray(R.array.menu_array)[i]);
            m.put(ATTRIBUTE_NAME_IMAGE, img[i]);
            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE};
        int[] to = {R.id.tvText, R.id.ivImg};
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.menu_item,
                from, to);

        mDrawerList.setAdapter(sAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon TO behave as action TO toggle nav drawer
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
                nativeCurrency = mBalances.get(arg0).currencyId;

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment != null && fragment instanceof DashFragment) {
                    ((DashFragment) fragment).refreshDashboard();
                }
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
                R.drawable.ic_drawer,  /* nav drawer image TO replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
//                tvABName.setText(mTitle);
//                if (currentFragment == FRAGMENT_DASH) {
//                	tvABRub.setText("B");
//                }
                invalidateOptionsMenu(); // creates call TO onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
//                tvABName.setText(mDrawerTitle);
//                tvABRub.setText("");
                invalidateOptionsMenu(); // creates call TO onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
    }

    void loadProfile() {
        new ApiRequestTask<Profile>() {

            @Override
            protected void doRequest(Callback<Profile> callback) {
                NetworkUtils.getInstance().createRestAdapter().create(ApiProfile.class).getProfile(callback);
            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return MenuActivity.this;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {
                CharSequence errText = error.getErrorDescription(getText(R.string.network_error));
                Toast toast = Toast.makeText(getContainerActivity(), errText, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 50);
                toast.show();
            }

            @Override
            protected void onCancelled() {
            }

            @Override
            protected void onSuccess(Profile profile, Response response) {
                Profile.Attribute title = profile.findTitle();
                Profile.Attribute url = profile.findMerchantUrl();
                Profile.Attribute logo = profile.findMerchantLogo();

                if (logo == null || TextUtils.isEmpty(logo.displayValue)) {
                    Picasso.with(MenuActivity.this)
                            .load(R.drawable.no_avatar)
                            .transform(CircleTransformation.getInstance())
                            .into(ivAccountIcon);
                } else {
                    Picasso.with(MenuActivity.this)
                            .load(logo.displayValue)
                            .transform(CircleTransformation.getInstance())
                            .into(ivAccountIcon);
                }

                tvName.setText(title == null ? "" : title.displayValue);
                tvUrl.setText(url == null ? "" : url.displayValue);
                mIsBusinessAccount = "Business".equals(profile.accountTypeId);
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getNativeCurrency() {
        return nativeCurrency;
    }

    @Override
    public void onDrawerListChanged() {
        ((SimpleAdapter)mDrawerList.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean ismIsBusinessAccount() {
        return mIsBusinessAccount;
    }

    @Override
    public void startProgress() {
        startPBAnim();
    }

    @Override
    public void stopProgress() {
        stopPBAnim();
    }

    @Override
    public void onBalanceLoaded(List<Balance> balances) {
        mBalances.clear();
        mBalances.addAll(balances);

        if (!mBalances.isEmpty()) {
            String nativeCurrency = balances.isEmpty() ? "643" : balances.get(0).currencyId;
            for (Balance balance : balances) {
                if (balance.isNative) {
                    nativeCurrency = balance.currencyId;
                    break;
                }
            }
            this.nativeCurrency = nativeCurrency;
            if (DBG) Log.v(TAG, "native currency: " + nativeCurrency);
            refreshDashCurrencyViewPager();
        }
    }

    @Override
    public View getPopupAnchorView() {
        return progressBar;
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
        if (position == 1) {
            currentFragment = FRAGMENT_DASH;
            changeFragment(fragmentDash);
        } else if (position == 2) {
            currentFragment = FRAGMENT_USERENTRY;
            Fragment fragmentUserEntry = new UserEntryFragment();
            changeFragment(fragmentUserEntry);
        } else if (position == 3) {
            currentFragment = FRAGMENT_INVOICE;
            Fragment fragmentInvoice = new InvoiceFragment();
            changeFragment(fragmentInvoice);
        } else if (position == 4) {
            currentFragment = 0;
            Fragment fragmentTemplate = new TemplateFragment();
            changeFragment(fragmentTemplate);
        } else if (position == 5) {
            Intent intent = new Intent(this, TicketListActivity.class);
            startActivity(intent);
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        } else if (position == 6) {
            new DialogExit().show(getFragmentManager(), "dlgExit");
        }

        // update selected item and title, then close the drawer

        mDrawerList.setItemChecked(position, true);

        if (position == 1) {
            refreshDashCurrencyViewPager();
        } else {
            ArrayList<String> abName = new ArrayList<>();
            ArrayList<String> abRubl = new ArrayList<>();
            abName.add(getResources().getStringArray(R.array.menu_array)[position - 1]);
            abRubl.add("");
            PagerAdapter currencyPagerAdapter = new ViewPagerAdapter(this,
                    abName, abRubl);
            vpCurrency.setAdapter(currencyPagerAdapter);
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void refreshDashCurrencyViewPager() {
        // TODO Переписать весь этот пиздец
        if (currentFragment != FRAGMENT_DASH) return;
        if (!mBalances.isEmpty()) {
            ArrayList<String> currSumNames = new ArrayList<>(mBalances.size());
            ArrayList<String> currRubls = new ArrayList<>(mBalances.size());

            for (Balance balance : mBalances) {
                long amount = balance.amount.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                if ("643".equals(balance.currencyId)) {
                    currSumNames.add(getString(R.string.balance) + " "
                            + TextUtilsW1.formatNumber(amount));
                    currRubls.add("B");
                } else {
                    if (amount != 0) {
                        currSumNames.add(getString(R.string.balance) + " " +
                                TextUtilsW1.formatNumber(amount) + " " + TextUtilsW1.getCurrencySymbol(balance.currencyId));
                        currRubls.add("");
                    }
                }
            }

            PagerAdapter currencyPagerAdapter = new ViewPagerAdapter(this,
                    currSumNames, currRubls);
            vpCurrency.setAdapter(currencyPagerAdapter);
        }
    }

    private void changeFragment(Fragment targetFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change TO the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //dlgExit.show(getFragmentManager(), "dlgExit");
        mDrawerLayout.closeDrawer(mDrawerList);
        if (currentFragment != FRAGMENT_DASH) {
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
        NetworkUtils.getInstance().createRestAdapter().create(ApiSessions.class).logout(new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                Session.getInstance().clear();
            }

            @Override
            public void failure(RetrofitError error) {
                Session.getInstance().clear();
            }
        });
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
        if (mBalances.isEmpty()) return "";
        List<String> amounts = new ArrayList<>(mBalances.size());
        for (Balance balance: mBalances) {
            Number amount = balance.holdAmount.setScale(0, BigDecimal.ROUND_UP);
            if (amount.longValue() > 0) amounts.add(TextUtilsW1.formatNumber(amount) + " " + TextUtilsW1.getCurrencySymbol(balance.currencyId));
        }
        if (amounts.isEmpty()) return "";
        return TextUtils.join(", ", amounts);
    }

    public int getCurrentFragment() {
        return currentFragment;
    }
}