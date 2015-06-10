package com.w1.merchant.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.w1.merchant.android.R;

/**
 * Created by alexey on 27.02.15.
 */
public class CheckboxStyleSegmentedRadioGroup extends LinearLayout {

    private int mCheckedId = -1;

    private boolean mProtectFromCheckedChange;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckboxStyleSegmentedRadioGroup(Context context) {
        super(context);
    }

    public CheckboxStyleSegmentedRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckboxStyleSegmentedRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckboxStyleSegmentedRadioGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CheckboxStyleSegmentedRadioGroup group, int checkedId);
    }

    public void check(int id) {
        // don't even bother
        if (id != -1 && (id == mCheckedId)) {
            return;
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    public int getCheckedRadioButtonId() {
        return mCheckedId;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        changeButtonsImages();
        setupCheckedStateTracker();

        int count = super.getChildCount();
        for (int i = 0; i < count; ++i) {
            if (getChildAt(i) instanceof CheckboxStyleRadioButton && ((CheckboxStyleRadioButton) getChildAt(i)).isChecked()) {
                mCheckedId = getChildAt(i).getId();
                break;
            }
        }
    }

    private void changeButtonsImages(){
        int count = super.getChildCount();

        if(count > 1){
            super.getChildAt(0).setBackgroundResource(R.drawable.segment_radio_left);
            for(int i=1; i < count-1; i++){
                super.getChildAt(i).setBackgroundResource(R.drawable.segment_radio_middle);
            }
            super.getChildAt(count-1).setBackgroundResource(R.drawable.segment_radio_right);
        }else if (count == 1){
            super.getChildAt(0).setBackgroundResource(R.drawable.segment_button);
        }
    }

    private void setupCheckedStateTracker() {
        CheckedStateTracker tracker = new CheckedStateTracker();

        int count = super.getChildCount();
        for (int i = 0; i < count; ++i) {
            if (getChildAt(i) instanceof CheckboxStyleRadioButton) {
                ((CheckboxStyleRadioButton)getChildAt(i)).setOnCheckedChangeListener(tracker);
            }
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    private void setCheckedId(int id) {
        mCheckedId = id;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return;
            }

            mProtectFromCheckedChange = true;
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false);
            }
            mProtectFromCheckedChange = false;

            int id = buttonView.getId();
            setCheckedId(isChecked ? id : -1);
        }
    }

}
