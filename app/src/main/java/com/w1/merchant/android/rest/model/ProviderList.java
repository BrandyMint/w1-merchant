package com.w1.merchant.android.rest.model;


import java.util.List;

public class ProviderList {

    /**
     * поставщики услуг
     */
    public List<Provider> providers;

    /**
     * Общее число поставщиков услуг, удовлетворяющих заданным критериям
     */
    public int totalCount;
}
