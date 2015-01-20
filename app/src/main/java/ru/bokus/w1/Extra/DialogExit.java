package ru.bokus.w1.Extra;

import ru.bokus.w1.Activity.MenuActivity;
import ru.bokus.w1.Activity.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

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
