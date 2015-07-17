package com.w1.merchant.android.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.w1.merchant.android.R;

public class ConfirmPaymentActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        TextView tvConfirmText = (TextView) findViewById(R.id.tvConfirmText);
        tvConfirmText.setText(getString(R.string.transact_proces,
                getIntent().getStringExtra("sum") + " C"));

        findViewById(R.id.tvBack).setOnClickListener(myOnClickListener);
        findViewById(R.id.ivBack).setOnClickListener(myOnClickListener);
    }

    private final OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}

