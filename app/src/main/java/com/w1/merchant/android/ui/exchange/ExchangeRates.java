package com.w1.merchant.android.ui.exchange;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.w1.merchant.android.rest.model.ExchangeRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by alexey on 09.07.15.
 */
class ExchangeRates implements Parcelable {

    private final ArrayList<ExchangeRate> mList;

    private final Map<String, List<ExchangeRate>> mSrcCurrency;

    public static final ExchangeRates EMPTY = new ExchangeRates(Collections.<ExchangeRate>emptyList());

    public ExchangeRates(List<ExchangeRate> list) {
        mList = new ArrayList<>(list);
        mSrcCurrency = getSrcCurrencyMap(list);
    }

    private static Map<String, List<ExchangeRate>> getSrcCurrencyMap(List<ExchangeRate> list) {
        HashMap<String, List<ExchangeRate>> map = new HashMap<>();
        for (ExchangeRate rate: list) {
            String key = rate.srcCurrencyId;
            List<ExchangeRate> subList = map.get(key);
            if (subList == null) {
                subList = new ArrayList<>();
                map.put(key, subList);
            }
            subList.add(rate);
        }
        for (Map.Entry<String, List<ExchangeRate>> entry : map.entrySet()) {
            map.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    private List<ExchangeRate> getListByCurrencyFrom(String currencyFrom) {
        List<ExchangeRate> list = mSrcCurrency.get(currencyFrom);
        if (list == null) return Collections.emptyList();
        return list;
    }

    public LinkedHashSet<String> getDstCurrencies(String currencyFrom) {
        List<ExchangeRate> exchangeRates = getListByCurrencyFrom(currencyFrom);
        LinkedHashSet<String> ratesSet = new LinkedHashSet<>(exchangeRates.size());
        for (ExchangeRate rate: exchangeRates) ratesSet.add(rate.dstCurrencyId);
        return ratesSet;
    }

    public boolean containsSrcCurrency(String id) {
        return mSrcCurrency.containsKey(id);
    }

    public boolean isEmpty() {
        return mList.isEmpty();
    }

    @Nullable
    public ExchangeRate getRate(@Nullable String currencyFrom, @Nullable String currencyTo) {
        if (currencyFrom == null || currencyTo == null) return null;
        for (ExchangeRate rate: getListByCurrencyFrom(currencyFrom)) {
            if (rate.dstCurrencyId.equals(currencyTo)) return rate;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mList);
    }

    protected ExchangeRates(Parcel in) {
        this.mList = new ArrayList<>();
        in.readList(this.mList, List.class.getClassLoader());
        mSrcCurrency = getSrcCurrencyMap(mList);
    }

    public static final Parcelable.Creator<ExchangeRates> CREATOR = new Parcelable.Creator<ExchangeRates>() {
        public ExchangeRates createFromParcel(Parcel source) {
            return new ExchangeRates(source);
        }

        public ExchangeRates[] newArray(int size) {
            return new ExchangeRates[size];
        }
    };
}
