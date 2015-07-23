package com.w1.merchant.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.w1.merchant.android.utils.CurrencyHelper;

import java.util.List;

/**
 * Адаптер для выпадающего списка валют
 */
public class CurrencyAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private final List<String> mCurrencyList;

    private final int mDropdownLayoutId;

    private final int mItemLayoutId;

    /**
     * Адаптер для выпадающего списка валют
     * @param context Контекст
     * @param currencyList Список с балансами. Список общий, новый не создается, хранится ссылка
     */
    public CurrencyAdapter(Context context, List<String> currencyList) {
        this(context, currencyList, android.R.layout.simple_spinner_item,
                android.R.layout.simple_spinner_dropdown_item);
    }

    public CurrencyAdapter(Context context, List<String> currencyList, int itemLayoutId, int dropdownLayoutId) {
        mCurrencyList = currencyList;
        mInflater = LayoutInflater.from(context);
        mItemLayoutId = itemLayoutId;
        mDropdownLayoutId = dropdownLayoutId;
    }

    @Override
    public int getCount() {
        return mCurrencyList.size();
    }

    @Override
    public String getItem(int position) {
        return mCurrencyList.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root;
        if (convertView == null) {
            root = mInflater.inflate(mItemLayoutId, parent, false);
        } else {
            root = convertView;
        }
        TextView textView = (TextView) root.findViewById(android.R.id.text1);
        textView.setText(CurrencyHelper.getCurrencyName(getItem(position), root.getResources()));

        return root;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View root;
        if (convertView == null) {
            root = mInflater.inflate(mDropdownLayoutId, parent, false);
        } else {
            root = convertView;
        }
        TextView textView = (TextView) root.findViewById(android.R.id.text1);
        textView.setText(CurrencyHelper.getCurrencyName(getItem(position), root.getResources()));

        return root;
    }
}
