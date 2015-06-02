package com.w1.merchant.android.rest.model;

import java.util.List;

/**
 * Created by alexey on 08.02.15.
 */
public class Invoices {

    public List<Invoice> invoices;

    /**
     * Общее число счетов, удовлетворяющих заданным критериям
     */
    public int totalCount;
}
