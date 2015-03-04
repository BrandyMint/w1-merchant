package com.w1.merchant.android.extra;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.w1.merchant.android.R;

public class DialogNoInet extends DialogFragment implements OnClickListener{
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
	        .setTitle(R.string.warning)
	        .setPositiveButton(R.string.yes, this)
	        .setMessage(R.string.no_inet);
	    return adb.create();
	    
	  }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch (which) {
	    case Dialog.BUTTON_POSITIVE:
	    	getActivity().finish();
	    	break;
		}
	}

}
