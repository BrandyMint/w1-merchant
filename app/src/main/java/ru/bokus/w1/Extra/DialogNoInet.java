package ru.bokus.w1.Extra;

import ru.bokus.w1.Activity.LoginActivity;
import ru.bokus.w1.Activity.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

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
	    	((LoginActivity)getActivity()).finish();
	    	break;
		}
	}

}
