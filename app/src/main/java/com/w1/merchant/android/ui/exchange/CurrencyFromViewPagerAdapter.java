package com.w1.merchant.android.ui.exchange;

import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Balance;
import com.w1.merchant.android.ui.widget.ViewPagerAdapter;
import com.w1.merchant.android.utils.text.CurrencyFormattingTextWatcher;
import com.w1.merchant.android.utils.text.CurrencyKeyListener;
import com.w1.merchant.android.utils.text.TextWatcherWrapper;
import com.w1.merchant.android.utils.CurrencyHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexey on 11.07.15.
 */
final class CurrencyFromViewPagerAdapter extends PagerAdapter {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = "CurrencyFromVPAdapter";

    private final Callbacks mCallbacks;

    private List<String> mCurrencyList = new ArrayList<>();

    public interface Callbacks {
        Collection<Balance> getBalanceList();

        CharSequence getUserAmountFrom(String currencyFrom);

        void onAmountFromTextChanged(Editable s);
    }

    public CurrencyFromViewPagerAdapter(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void setCurrencyList(List<String> currencyList) {
        mCurrencyList.clear();
        mCurrencyList.addAll(currencyList);
        notifyDataSetChanged();
    }

    public List<String> getCurrencyList() {
        return mCurrencyList;
    }

    public String getCurrency(int position) {
        return mCurrencyList.get(position);
    }

    @Override
    public int getCount() {
        return mCurrencyList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewHolder holder;
        String currency = getCurrency(position);
        if (DBG) Log.v(TAG, "instantiateItem pos: " + position + " currency: " + currency);

        holder = findViewHolder(container, currency);
        if (holder != null) {
            Log.d(TAG, "instantiateItem: found old viewHolder");
            bindViewHolder(holder);
            return holder;
        }

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = inflater.inflate(R.layout.item_exchange_currency, container, false);
        holder = new ViewHolder(root, currency);

        holder.root.setTag(R.id.exchange_from_view_holder, holder);
        holder.amount.setTag(R.id.exchange_edit_text_from_currency, holder);

        SpannableString hint = new SpannableString(container.getResources().getText(R.string.enter_the_amount));
        hint.setSpan(new RelativeSizeSpan(0.6f), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.amount.setHint(hint);
        holder.amount.setKeyListener(new CurrencyKeyListener(currency, true));
        holder.amount.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(11)});

        holder.amount.addTextChangedListener(new CurrencyFormattingTextWatcher(currency, true));
        holder.amount.addTextChangedListener(mTextWatcher);
        final ViewHolder finalHolder = holder;
        final Runnable refreshAmountColorRunnable = new Runnable() {
            @Override
            public void run() {
                refreshAmountColor(finalHolder);
            }
        };
        holder.amount.addTextChangedListener(new TextWatcherWrapper(new TextWatcherWrapper.OnTextChangedListener() {
            @Override
            public void onTextChanged(Editable s) {
                finalHolder.amount.removeCallbacks(refreshAmountColorRunnable);
                finalHolder.amount.post(refreshAmountColorRunnable);
            }
        }));
        holder.amount.setHorizontallyScrolling(false);

        bindViewHolder(holder);

        container.addView(root);

        return holder;
    }

    @Override
    public int getItemPosition(Object object) {
        int position = mCurrencyList.indexOf(((ViewHolder) object).currency);
        if (position < 0) return ViewPagerAdapter.POSITION_NONE;
        return position;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ViewHolder) object).root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewHolder holder = (ViewHolder) object;
        container.removeView(holder.root);
    }

    public void onPageSelected(ViewGroup container, int position) {
        Log.d(TAG, "onPageSelected() called with " + "position = [" + position + "]");
        String currency = getCurrency(position);
        if (currency == null) return;
        ViewHolder holder = findViewHolder(container, currency);
        if (holder != null) {
            bindViewHolder(holder);
        }
    }

    public void refreshAmount(ViewGroup container, int position) {
        String currency = getCurrency(position);
        if (currency == null) return;
        ViewHolder holder = findViewHolder(container, currency);
        if (holder != null) {
            bindAmount(holder);
        }
    }

    void bindViewHolder(ViewHolder holder) {
        Log.d(TAG, "bindViewHolder() called with " + "holder = [" + holder + "]");
        holder.title.setText(ExchangesHelper.getBalanceCardTitle(mCallbacks.getBalanceList(),
                holder.currency, holder.root.getResources()));
        bindAmount(holder);
    }

    void bindAmount(ViewHolder holder) {
        mTextWatcher.disable();
        try {
            CharSequence text = mCallbacks.getUserAmountFrom(holder.currency);
            if (!TextUtils.equals(holder.amount.getText(), text)) {
                holder.amount.setText(text);
                holder.amount.setSelection(holder.amount.length());
            }
        } finally {
            mTextWatcher.enable();
        }
    }

    private void refreshAmountColor(ViewHolder holder) {
        final int colorResId;
        Log.d(TAG, "refreshAmountColor()");
        if (isAmountWithinLimits(holder.currency, holder.amount.getText(), mCallbacks.getBalanceList())) {
            colorResId = android.R.color.white;
        } else {
            colorResId = R.color.text_color_invalid_amount;
        }
        holder.amount.setTextColor(holder.amount.getResources().getColor(colorResId));
    }

    @Nullable
    private ViewHolder findViewHolder(ViewGroup container, String currency) {
        for (int i = container.getChildCount() - 1; i >= 0; --i) {
            View root = container.getChildAt(i);
            ViewHolder holder = (ViewHolder) root.getTag(R.id.exchange_from_view_holder);
            if (holder != null && currency.equals(holder.currency)) return holder;
        }
        return null;
    }

    private static boolean isAmountWithinLimits(String currencyId, CharSequence amount, Collection<Balance> balances) {
        if (TextUtils.isEmpty(amount)) return true;
        Balance balance = ExchangesHelper.findBalance(balances, currencyId);
        if (balance == null) return true;
        BigDecimal amountDecimal = CurrencyHelper.parseAmount(amount);
        return amountDecimal == null || balance.amount.compareTo(amountDecimal) >= 0;
    }

    private final TextWatcherWrapper mTextWatcher = new TextWatcherWrapper(new TextWatcherWrapper.OnTextChangedListener() {
        @Override
        public void onTextChanged(Editable s) {
            mCallbacks.onAmountFromTextChanged(s);
        }
    });

    private static class ViewHolder {
        public final View root;
        public final TextView title;
        public final EditText amount;
        public final String currency;

        public ViewHolder(View root, String currency) {
            this.root = root;
            this.title = (TextView) root.findViewById(R.id.exchange_title_card);
            this.amount = (EditText) root.findViewById(R.id.exchange_edit_card);
            this.currency = currency;
        }

        @Override
        public String toString() {
            return "ViewHolder{" +
                    "currency='" + currency + '\'' +
                    '}';
        }
    }
}
