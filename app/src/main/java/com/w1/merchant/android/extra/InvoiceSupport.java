package com.w1.merchant.android.extra;

import android.content.Context;
import android.text.TextUtils;

import com.w1.merchant.android.activity.MenuActivity;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.request.GETInvoice;

public class InvoiceSupport {
	Context mCtx;

	public InvoiceSupport(Context context) {
		mCtx = context;
	}

	public void getData(String state, int pageNumber,
			String search, String token) {
		String[] requestData = { "", "", "", "" };
	    GETInvoice getInvoice;
	    
		//запрос списка счетов
	    requestData[0] = String.format(Constants.URL_INVOICES, pageNumber);
        if (state.equals("Accepted")) {
        	requestData[0] += "&invoiceStateId=Accepted"; 
		} else if (state.equals("Created")) {
        	requestData[0] += "&invoiceStateId=Created";
		} else if (state.equals("HasSuspense")) {
        	requestData[0] += "&invoiceStateId=Created";
        	requestData[2] = "HasSuspense";
		}
        if (!TextUtils.isEmpty(search)) {
        	requestData[0] += "&searchString=" + search;
        }
        requestData[1] = token;
        requestData[3] = ((MenuActivity) mCtx).nativeCurrency;
        getInvoice = new GETInvoice(mCtx);
        getInvoice.execute(requestData);
	}
}
