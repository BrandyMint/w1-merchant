package com.w1.merchant.android.extra;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.w1.merchant.android.R;
import com.w1.merchant.android.model.TransactionHistory;
import com.w1.merchant.android.service.ApiRequestTask;
import com.w1.merchant.android.service.ApiUserEntry;
import com.w1.merchant.android.utils.NetworkUtils;

import java.util.Calendar;

import retrofit.Callback;
import retrofit.client.Response;

public class RequestSummaryDialogFragment extends DialogFragment {

    private DatePicker mDatepicker;
    private TextView mBack;
    private TextView mDate;
    private int current = 0;
    private int day0, month0, year0;
    private int day1, month1, year1;

    private ApiUserEntry mApiUserEntry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        mApiUserEntry = NetworkUtils.getInstance().createRestAdapter().create(ApiUserEntry.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.popup_date, container, false);

        Calendar today = Calendar.getInstance();
        day1 = today.get(Calendar.DAY_OF_MONTH);
        month1 = today.get(Calendar.MONTH);
        year1 = today.get(Calendar.YEAR);

        today.add(Calendar.MONTH, -1);
        day0 = today.get(Calendar.DAY_OF_MONTH);
        month0 = today.get(Calendar.MONTH);
        year0 = today.get(Calendar.YEAR);

        mDatepicker = (DatePicker) root.findViewById(R.id.dp1);
        mDatepicker.init(year0, month0, day0, null);

        mBack = (TextView) root.findViewById(R.id.tvBack);
        mDate = (TextView) root.findViewById(R.id.tvDate);
        TextView tvNext = (TextView) root.findViewById(R.id.tvNext);

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current == 0) {
                    mBack.setText(R.string.go_back);
                    mDate.setText(R.string.end_date);
                    current += 1;
                    day0 = mDatepicker.getDayOfMonth();
                    month0 = mDatepicker.getMonth() + 1;
                    year0 = mDatepicker.getYear();
                    mDatepicker.init(year1, month1, day1, null);
                } else {
                    //запускаем итоги по выписке
                    current = 0;

                    dismissAllowingStateLoss();

                    day1 = mDatepicker.getDayOfMonth();
                    month1 = mDatepicker.getMonth() + 1;
                    year1 = mDatepicker.getYear();

                    // TODO
                    /*
                    dashSupport.getDataTotal("" + year0 + "-" + month0 + "-" + day0,
                            "" + year1 + "-" + month1 + "-" + day1, "Inc",
                            Session.getInstance().getBearer(),
                            nativeCurrency,
                            getCurrPageUETotal() + "");
                            */
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current == 0) {
                    //выходим
                    current = 0;
                    dismissAllowingStateLoss();
                } else {
                    mBack.setText(R.string.cancel);
                    mDate.setText(R.string.begin_date);
                    mDatepicker.init(year0, month0, day0, null);
                    current -= 1;
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadSummary() {

        new ApiRequestTask<TransactionHistory>() {

            int pageNumber = 1;

            @Override
            protected void doRequest(Callback<TransactionHistory> callback) {

            }

            @Nullable
            @Override
            protected Activity getContainerActivity() {
                return null;
            }

            @Override
            protected void onFailure(NetworkUtils.ResponseErrorException error) {

            }

            @Override
            protected void onCancelled() {

            }

            @Override
            protected void onSuccess(TransactionHistory transactionHistory, Response response) {

            }
        }.execute();

    }

}
