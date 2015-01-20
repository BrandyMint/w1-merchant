package ru.bokus.w1.Extra;

import ru.bokus.w1.Request.GETInvoice;
import ru.bokus.w1.Activity.MenuActivity;
import ru.bokus.w1.Activity.R;
import android.content.Context;
import android.text.TextUtils;

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
	    requestData[0] = mCtx.getString(R.string.url_main) +
        		mCtx.getString(R.string.url_invoice, pageNumber + "");
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
