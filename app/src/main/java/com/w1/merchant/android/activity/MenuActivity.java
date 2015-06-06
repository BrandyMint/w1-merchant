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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.w1.merchant.android.rest.ResponseErrorException;
import com.w1.merchant.android.rest.RestClient;
import com.w1.merchant.android.rest.model.AuthModel;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.rest.model.Profile;
import com.w1.merchant.android.rest.service.ApiProfile;
import com.w1.merchant.android.support.TicketListActivity;
import com.w1.merchant.android.utils.RetryWhenCaptchaReady;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.viewextended.CircleTransformation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class MenuActivity extends ActivityBase implements UserEntryFragment.OnFragmentInteractionListener,
    InvoiceFragment.OnFragmentInteractionListener,
    DashFragment.OnFragmentInteractionListener,
    TemplateFragment.OnFragmentInteractionListener,
    IProgressbarProvider {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final int SELECT_PRINCIPAL_REQUEST_CODE = 1;

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

    private WeakHashMap<Object, Void> mProgressConsumers = new WeakHashMap<>();
    private View mProgressBar;
    private boolean mIsBusinessAccount = false;

    private NavDrawerMenu mNavDrawerMenu;

    private Subscription mProfileSubscription = Subscriptions.unsubscribed();

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //шапка меню
        ivAccountIcon = (ImageView) findViewById(R.id.ivAccountIcon);
        mProgressBar = findViewById(R.id.progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        fragmentDash = new DashFragment();

        setSupportActionBar(toolbar);
        initNavigationDrawer(savedInstanceState);

        setupCurrencyViewpagerCustomView();
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM
                );

        loadProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PRINCIPAL_REQUEST_CODE && resultCode == RESULT_OK) {
            AuthModel user = data.getParcelableExtra(SelectPrincipalActivity.RESULT_AUTH_USER);
            Session.getInstance().setAuth(user);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate(); // Закрывать все остальные активности?
                }
            }, 5 * 16);
        }
    }

    @SuppressLint("InflateParams")
    private void setupCurrencyViewpagerCustomView() {
        mCurrencyViewPager = (ViewPager)LayoutInflater.from(this).inflate(R.layout.action_bar_rubl2, null, false);
        mCurrencyViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        //noinspection ConstantConditions
        getSupportActionBar().setCustomView(mCurrencyViewPager);
    }

    void loadProfile() {
        mProfileSubscription.unsubscribe();

        ApiProfile apiProfile = RestClient.getApiProfile();
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
                        if (e instanceof ResponseErrorException) {
                            errText = ((ResponseErrorException) e).getErrorDescription(getText(R.string.network_error));
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavDrawerMenu.onSaveInstanceState(outState);
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
    public Object startProgress() {
        return startPBAnim();
    }

    @Override
    public void stopProgress(Object consumer) {
        stopPBAnim(consumer);
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
        return mProgressBar;
    }

    public void selectItem(int position) {

        switch (position) {
            case R.id.change_account_button:
                Intent selectPrincipalIntent = new Intent(MenuActivity.this, SelectPrincipalActivity.class);
                startActivityForResult(selectPrincipalIntent, SELECT_PRINCIPAL_REQUEST_CODE);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                return;
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
                new DialogExit().show(getSupportFragmentManager(), "dlgExit");
                break;
        }

        // TODO Смена фрагмента должна происходить после анимации закрытия бокового меню,
        // иначе эта анимация тармазит
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
        if (savedInstanceState == null) {
            mNavDrawerMenu.setActivatedItem(R.id.drawer_menu_dashboard);
            selectItem(R.id.drawer_menu_dashboard);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        refreshProgressBar();
        refreshCurrencyViewPager();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change TO the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        if (getCurrentFragment() != FRAGMENT_DASH) {
            selectItem(R.id.drawer_menu_dashboard);
            invalidateOptionsMenu();
        }
    }

    public void exit() {
        Session.getInstance().close();
        finish();
    }

    public Object startPBAnim() {
        Object token = new Object();
        mProgressConsumers.put(token, null);
        if (DBG) Log.v(TAG, "startPBAnim tokens: " + mProgressConsumers.size());
        refreshProgressBar();
        return token;
    }

    public void stopPBAnim(Object token) {
        mProgressConsumers.remove(token);
        if (DBG) Log.v(TAG, "stopPBAnim tokens: " + mProgressConsumers.size());
        refreshProgressBar();
    }

    public void refreshProgressBar() {
        if (!isFinishing()) {
            mProgressBar.setVisibility(mProgressConsumers.isEmpty() ? View.INVISIBLE : View.VISIBLE);
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

    private void initNavigationDrawer(Bundle savedInstanceState) {
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.drawer_title));

        mNavDrawerMenu = new NavDrawerMenu(mDrawerLayout, savedInstanceState, new NavDrawerMenu.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int itemId) {
                selectItem(itemId);
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call TO onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call TO onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.findViewById(R.id.change_account_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(v.getId());
            }
        });
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

        private static final String ARG_ACTIVATED_ITEM = "com.w1.merchant.android.activity.MenuActivity.NavDrawerMenu.ARG_ACTIVATED_ITEM";

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

        public NavDrawerMenu(View root, Bundle savedInstanceState, OnItemClickListener listener) {
            mListener = listener;
            mMenuItems = new View[MENU_VIEW_IDS.length];
            for (int i = 0; i < MENU_VIEW_IDS.length; ++i) {
                mMenuItems[i] = root.findViewById(MENU_VIEW_IDS[i]);
                if (mMenuItems[i] == null) throw new IllegalStateException();
            }
            for (View v: mMenuItems) v.setOnClickListener(mOnClickListener);
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(ARG_ACTIVATED_ITEM)) {
                    int itemId = savedInstanceState.getInt(ARG_ACTIVATED_ITEM);
                    setActivatedItem(itemId);
                }
            }
        }

        public void onSaveInstanceState(Bundle state) {
            Integer activatedItem = getActivatedItemId();
            if (activatedItem != null) state.putInt(ARG_ACTIVATED_ITEM, activatedItem);
        }

        public void setActivatedItem(int viewId) {
            for (View v: mMenuItems) v.setActivated(viewId == v.getId());
        }

        @Nullable
        public Integer getActivatedItemId() {
            Integer itemId = null;
            for (View v: mMenuItems) {
                if (v.isActivated()) {
                    itemId = v.getId();
                    break;
                }
            }
            return itemId;
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
            void onItemClicked(View view, int itemId);
        }

    }

}
