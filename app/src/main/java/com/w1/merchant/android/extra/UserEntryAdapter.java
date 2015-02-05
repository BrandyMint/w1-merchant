package com.w1.merchant.android.extra;

import android.content.Context;
import android.graphics.Color;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.w1.merchant.android.R;

import java.util.List;
import java.util.Map;

public class UserEntryAdapter extends SimpleAdapter {

    public static final String ATTRIBUTE_NAME_RUBL = "rubl";
    public static final String ATTRIBUTE_NAME_STATE = "state";
    public static final String ATTRIBUTE_NAME_DESCR = "descr";
    public static final String ATTRIBUTE_NAME_AMOUNT = "amount";
    public static final String ATTRIBUTE_NAME_IMAGE = "image";
    public static final String ATTRIBUTE_NAME_DATE = "date";
    public static final String ATTRIBUTE_NAME_NUMBER = "number";
    private static final String[] FROM = {ATTRIBUTE_NAME_NUMBER, ATTRIBUTE_NAME_DATE,
            ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_AMOUNT,
            ATTRIBUTE_NAME_DESCR, ATTRIBUTE_NAME_STATE,
            ATTRIBUTE_NAME_RUBL};

    private static final int[] TO = {R.id.tvNumber, R.id.tvDate, R.id.ivIcon,
                    R.id.tvAmount, R.id.tvDescr, R.id.tvState, R.id.tvRubl};

    public UserEntryAdapter(Context context, List<? extends Map<String, ?>> data) {
      super(context, data, R.layout.invoice_item, FROM, TO);
    }

    @Override
    public void setViewText(TextView v, String text) {
        // метод супер-класса, который вставляет текст
        super.setViewText(v, text);
        // если нужный нам TextView, то разрисовываем 
        if ((v.getId() == R.id.tvAmount) | (v.getId() == R.id.tvRubl)) {
        	if (text.startsWith("-")) {
        		v.setText(text.substring(1));	
        		v.setTextColor(Color.RED); 
        	} else if (text.startsWith("+")) {
        		v.setText(text.substring(1));	
        		v.setTextColor(Color.parseColor("#FFA500")); 
        	} else v.setTextColor(Color.parseColor("#9ACD32"));
        }	
//        } else if (v.getId() == R.id.tvRubl) {
//        	v.setText("A");
//        	if (text.substring(0, 1).equals("-")) {
//        		v.setTextColor(Color.RED);
//        	} else if (text.substring(0, 1).equals("+")) {
//        		v.setTextColor(Color.parseColor("#FFA500"));
//        	} else {
//        		v.setTextColor(Color.parseColor("#9ACD32"));
//        	}
//        }
    }

    
  }
