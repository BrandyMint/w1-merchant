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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.w1.merchant.android.Application;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.extra.DialogExit;
import com.w1.merchant.android.model.Balance;
import com.w1.merchant.android.model.Profile;
import com.w1.merchant.android.service.ApiProfile;
import com.w1.merchant.android.service.ApiSessions;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.support.TicketListActivity;
import com.w1.merchant.android.utils.NetworkUtils;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;
import com.w1.merchant.android.viewextended.CircleTransformation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class MenuActivity extends FragmentActivity implements UserEntryFragment.OnFragmentInteractionListener,
    InvoiceFragment.OnFragmentInteractionListener,
    DashFragment.OnFragmentInteractionListener,
    TemplateFragment.OnFragmentInteractionListener
{
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mCurrencyViewPager;
    private CurrencyViewPagerAdapter mCurrencyPagerAdapter;

    private static final int FRAGMENT_USERENTRY = 1;
    private static final int FRAGMENT_INVOICE = 2;
    public static final int FRAGMENT_DASH = 3;

    private ImageView ivAccountIcon;

    private List<Balance> mBalances = new ArrayList<>();
    public String mCurrency = "643";

    private DashFragment fragmentDash;

    private int totalReq = 0;
    private ProgressBar progressBar;
    private boolean mIsBusinessAccount = false;

    private NavDrawerMenu mNavDrawerMenu;

    private Subscription mProfileSubscription = Subscriptions.unsubscribed();

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
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.INVISIBLE);

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
        mNavDrawerMenu = new NavDrawerMenu(mDrawerLayout, new NavDrawerMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int itemId) {
                selectItem(itemId);
            }
        });

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        //шапка меню
        ivAccountIcon = (ImageView) findViewById(R.id.ivAccountIcon);

        // enable ActionBar app icon TO behave as action TO toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);

        mCurrencyViewPager = (ViewPager)LayoutInflater.from(this).inflate(R.layout.action_bar_rubl2, null, false);
        mCurrencyViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                //меняем валюту
                mCurrency = mBalances.get(arg0).currencyId;
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
        mCurrencyPagerAdapter = new CurrencyViewPagerAdapter();
        mCurrencyViewPager.setAdapter(mCurrencyPagerAdapter);
        getActionBar().setCustomView(mCurrencyViewPager);

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
            mNavDrawerMenu.setActivatedItem(R.id.drawer_menu_dashboard);
            selectItem(R.id.drawer_menu_dashboard);
        }
    }

    void loadProfile() {
        mProfileSubscription.unsubscribe();

        ApiProfile apiProfile = NetworkUtils.getInstance().createRestAdapter().create(ApiProfile.class);
        Observable<Profile> observable = AppObservable.bindActivity(this, apiProfile.getProfile());

        mProfileSubscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWhenCaptchaReady(this))
                .subscribe(new Observer<Profile>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        CharSequence errText;
                        if (e instanceof NetworkUtils.ResponseErrorException) {
                            errText = ((NetworkUtils.ResponseErrorException) e).getErrorDescription(getText(R.string.network_error));
                        } else {
                            errText = getText(R.string.network_error);
                        }
                        Toast toast = Toast.makeText(MenuActivity.this, errText, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }

                    @Override
                    public void onNext(Profile profile) {
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

                        ((TextView) findViewById(R.id.tvName)).setText(title == null ? "" : title.displayValue);
                        ((TextView) findViewById(R.id.account_id)).setText(TextUtilsW1.formatUserId(profile.userId));
                        ((TextView) findViewById(R.id.tvUrl)).setText(url == null ? "" : url.displayValue);
                        mIsBusinessAccount = "Business".equals(profile.accountTypeId);
                    }
                });
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
    protected void onDestroy() {
        super.onDestroy();
        mProfileSubscription.unsubscribe();
    }

    @Override
    public String getCurrency() {
        return mCurrency;
    }

    @Override
    public boolean isBusinessAccount() {
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
        boolean nativeCurrencyInitialized = !mBalances.isEmpty();
        mBalances.clear();
        for (Balance b: balances) if (!"Undefined".equalsIgnoreCase(b.visibilityType)) mBalances.add(b);

        if (!mBalances.isEmpty()) {
            String nativeCurrency = balances.isEmpty() ? "643" : balances.get(0).currencyId;
            for (Balance balance : balances) {
                if (balance.isNative) {
                    nativeCurrency = balance.currencyId;
                    break;
                }
            }
            if (!nativeCurrencyInitialized) this.mCurrency = nativeCurrency;
            if (DBG) Log.v(TAG, "native currency: " + nativeCurrency);
            refreshCurrencyViewPager();
        }
    }

    @Override
    public View getPopupAnchorView() {
        return progressBar;
    }

    public void selectItem(int position) {

        switch (position) {
            case R.id.drawer_menu_dashboard:
                changeFragment(fragmentDash);
                mNavDrawerMenu.setActivatedItem(position);
                sendScreenName(position);
                break;
            case R.id.drawer_menu_statement:
                Fragment fragmentUserEntry = new UserEntryFragment();
                changeFragment(fragmentUserEntry);
                mNavDrawerMenu.setActivatedItem(position);
                sendScreenName(position);
                break;
            case R.id.drawer_menu_invoices:
                Fragment fragmentInvoice = new InvoiceFragment();
                changeFragment(fragmentInvoice);
                mNavDrawerMenu.setActivatedItem(position);
                sendScreenName(position);
                break;
            case R.id.drawer_menu_withdrawal:
                Fragment fragmentTemplate = new TemplateFragment();
                changeFragment(fragmentTemplate);
                mNavDrawerMenu.setActivatedItem(position);
                sendScreenName(position);
                break;
            case R.id.drawer_menu_support:
                Intent intent = new Intent(this, TicketListActivity.class);
                startActivity(intent);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                return;
            case R.id.drawer_menu_logout:
                new DialogExit().show(getFragmentManager(), "dlgExit");
                break;
        }

        mDrawerLayout.closeDrawer(Gravity.LEFT);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshCurrencyViewPager();
            }
        });

    }

    private void sendScreenName(int menuId) {
        Tracker tracker = ((Application) getApplication()).getTracker();
        tracker.setScreenName(getString(NavDrawerMenu.getMenuItemTitle(menuId)));
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    private int findCurrentCurrencyPosition() {
        int size = mBalances.size();
        for (int position = 0; position < size; position += 1) {
            if (TextUtils.equals(mBalances.get(position).currencyId, mCurrency)) return position;
        }
        return 0;
    }

    private CharSequence getCurrentItemTitle() {
        int resId = mNavDrawerMenu.getCurrentMenuItemTitle();
        return resId <= 0 ? "" : getText(resId);
    }

    private void refreshCurrencyViewPager() {
        if (mBalances.size() == 0 || getCurrentFragment() != FRAGMENT_DASH) {
            mCurrencyPagerAdapter.setShowTitle(getCurrentItemTitle());
        } else {
            mCurrencyPagerAdapter.setBalances(mBalances);
            mCurrencyPagerAdapter.setShowBalance();
            mCurrencyViewPager.setCurrentItem(findCurrentCurrencyPosition());
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
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        if (getCurrentFragment() != FRAGMENT_DASH) {
            selectItem(R.id.drawer_menu_dashboard);
            invalidateOptionsMenu();
        }
    }

    public void exit() {
        closeSession();
        finish();
    }


    //закрытие сессии
    void closeSession() {
        Observable<Void> observer =
                NetworkUtils.getInstance().createRestAdapter().create(ApiSessions.class).logout();
        observer
                .subscribeOn(AndroidSchedulers.mainThread())
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        Session.getInstance().clear();
                    }
                })
                .subscribe();
    }

    public void startPBAnim() {
        totalReq += 1;
        if (totalReq == 1) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void stopPBAnim() {
        totalReq -= 1;
        if (totalReq == 0) {
            progressBar.setVisibility(View.INVISIBLE);
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
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (f == null) {
            return 0;
        } else  if (f instanceof DashFragment) {
            return FRAGMENT_DASH;
        } else if (f instanceof InvoiceFragment) {
            return FRAGMENT_INVOICE;
        } else if (f instanceof UserEntryFragment) {
            return FRAGMENT_USERENTRY;
        }
        return 0;
    }

    private final class CurrencyViewPagerAdapter extends PagerAdapter {

        private List<Balance> mBalances = new ArrayList<>();

        private boolean mShowTitle = true;

        private CharSequence mTitle = "";

        public CurrencyViewPagerAdapter() {
        }

        public void setShowTitle(CharSequence title) {
            if (!(mShowTitle && TextUtils.equals(mTitle, title))) {
                if (DBG) Log.v(TAG, "setShowTitle " + title);
                mShowTitle = true;
                mTitle = title;
                notifyDataSetChanged();
            }
        }

        public void setShowBalance() {
            if (mShowTitle) {
                if (DBG) Log.v(TAG, "setShowBalance ");
                mShowTitle = false;
                notifyDataSetChanged();
            }
        }

        public void setBalances(List<Balance> balances) {
            mBalances.clear();
            mBalances.addAll(balances);
            notifyDataSetChanged();
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mShowTitle ? 1 : mBalances.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            if (DBG) Log.v(TAG, "instantiateItem pos: " + position);

            LayoutInflater inflater = LayoutInflater.from(container.getContext());

            TextView tv = (TextView)inflater.inflate(R.layout.currency_viewpager_item, container, false);

            if (mShowTitle) {
                tv.setText(mTitle);
            } else {
                tv.setText(getCurrencyTitle(position));
            }

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((getCurrentFragment() == FRAGMENT_DASH) && !getWaitSum().isEmpty()) {
                        Toast toast = Toast.makeText(MenuActivity.this,
                                getString(R.string.awaiting) + " " + getWaitSum(),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 100);
                        toast.show();
                    }
                }
            });

            container.addView(tv);

            return tv;
        }

        private CharSequence getCurrencyTitle(int position) {
            Balance balance = mBalances.get(position);
            long amount = balance.amount.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

            SpannableStringBuilder sb = new SpannableStringBuilder(getText(R.string.balance));
            sb.append(" ");
            sb.append(TextUtilsW1.formatNumber(amount));
            sb.append(" ");
            sb.append(TextUtilsW1.getCurrencySymbol2(balance.currencyId, 1));
            return sb;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

    private static final class NavDrawerMenu {

        private final OnItemClickListener mListener;

        private static final int MENU_VIEW_IDS[] = new int[] {
                R.id.drawer_menu_dashboard,
                R.id.drawer_menu_statement,
                R.id.drawer_menu_invoices,
                R.id.drawer_menu_withdrawal,
                R.id.drawer_menu_support,
                R.id.drawer_menu_logout
        };

        private final View[] mMenuItems;

        public static int getMenuItemTitle(int viewId) {
            switch (viewId)  {
                case R.id.drawer_menu_dashboard: return R.string.title_dashboard;
                case R.id.drawer_menu_statement: return R.string.title_statement;
                case R.id.drawer_menu_invoices: return  R.string.title_invoices;
                case R.id.drawer_menu_withdrawal: return R.string.title_withdrawal;
                case R.id.drawer_menu_support: return R.string.title_support;
                case R.id.drawer_menu_logout: return R.string.title_logout;
                default:
                    return -1;
            }
        }

        public NavDrawerMenu(View root, OnItemClickListener listener) {
            mListener = listener;
            mMenuItems = new View[MENU_VIEW_IDS.length];
            for (int i = 0; i < MENU_VIEW_IDS.length; ++i) {
                mMenuItems[i] = root.findViewById(MENU_VIEW_IDS[i]);
                if (mMenuItems[i] == null) throw new IllegalStateException();
            }
            for (View v: mMenuItems) v.setOnClickListener(mOnClickListener);
        }

        public void setActivatedItem(int viewId) {
            for (View v: mMenuItems) v.setActivated(viewId == v.getId());
        }

        @Nullable
        public Integer getActivatedItemId() {
            for (View v: mMenuItems) {
                if (v.isActivated()) return v.getId();
            }
            return null;
        }

        public int getCurrentMenuItemTitle() {
            Integer id = getActivatedItemId();
            if (id == null) return -1;
            return getMenuItemTitle(id);
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isActivated()) return;
                mListener.onItemClicked(v, v.getId());
            }
        };

        public interface OnItemClickListener {
            public void onItemClicked(View view, int itemId);
        }

    }

}