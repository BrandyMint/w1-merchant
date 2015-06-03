package com.w1.merchant.android.extra;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.w1.merchant.android.R;
import com.w1.merchant.android.activity.MenuActivity;

public class DialogExit extends DialogFragment implements OnClickListener{
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
	        .setTitle(R.string.warning)
	        .setPositiveButton(R.string.yes, this)
	        .setNegativeButton(R.string.no, this)
	        .setMessage(R.string.exit_message);
	    return adb.create();
	    
	  }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch (which) {
	    case Dialog.BUTTON_POSITIVE:
	    	((MenuActivity)getActivity()).exit();
	    	break;
		}
	}

}
