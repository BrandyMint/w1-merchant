package com.w1.merchant.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.w1.merchant.android.R;

public class ConfirmInvoiceActivity extends ActivityBase {

    EditText etDescrText, etTelRecipient, etSum;
    TextView tvBack, tvConfirmText;
    ImageView ivBack;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        intent = getIntent();
        tvConfirmText = (TextView) findViewById(R.id.tvConfirmText);
        tvConfirmText.setText(
                getString(R.string.invoice_issued_successfully, intent.getStringExtra("sum")));

        tvBack = (TextView) findViewById(R.id.tvBack);
        tvBack.setOnClickListener(myOnClickListener);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(myOnClickListener);
    }

    OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}

