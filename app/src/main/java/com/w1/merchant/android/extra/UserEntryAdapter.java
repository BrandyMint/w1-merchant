package com.w1.merchant.android.extra;

import android.content.Context;
import android.graphics.Color;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.w1.merchant.android.R;

import java.util.List;
import java.util.Map;

public class UserEntryAdapter extends SimpleAdapter {

    public UserEntryAdapter(Context context,
        List<? extends Map<String, ?>> data, int resource,
        String[] from, int[] to) {
      super(context, data, resource, from, to);
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
