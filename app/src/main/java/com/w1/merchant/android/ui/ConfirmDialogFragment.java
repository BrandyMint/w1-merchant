package com.w1.merchant.android.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.w1.merchant.android.R;

/**
 * Created by alexey on 22.07.15.
 */
public class ConfirmDialogFragment extends DialogFragment {

    private static final String KEY_MESSAGE = "MESSAGE";

    private InteractionListener mListener;

    public interface InteractionListener {
        void onConfirmDialogDismissed();
    }

    public static ConfirmDialogFragment newInstance(String message) {
        Bundle args = new Bundle(1);
        args.putString(KEY_MESSAGE, message);
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(args);
        fragment.setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, R.style.Base_Theme_AppCompat_Light_DialogWhenLarge);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (InteractionListener) getActivity();
            if (mListener == null) throw new NullPointerException();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialog_confirm, container, false);
        TextView descriptionView = (TextView)root.findViewById(R.id.description);
        Toolbar toolbar = (Toolbar)root.findViewById(R.id.toolbar);
        View okButton = root.findViewById(R.id.button_ok);

        View.OnClickListener dismissClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        };

        toolbar.setNavigationOnClickListener(dismissClickListener);
        okButton.setOnClickListener(dismissClickListener);

        String text = getArguments().getString(KEY_MESSAGE);
        descriptionView.setText(text);

        return root;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) mListener.onConfirmDialogDismissed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
