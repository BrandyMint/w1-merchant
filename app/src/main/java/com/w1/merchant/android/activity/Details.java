package com.w1.merchant.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.model.Invoice;
import com.w1.merchant.android.model.TransactionHistoryEntry;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Date;

public class Details extends ActivityBase {
    private static final String TAG = Constants.LOG_TAG;
    private static final boolean DBG = BuildConfig.DEBUG;

    private static final String ARG_TRANSACTION_HISTORY_ENTRY = "com.w1.merchant.android.activity.Details2.ARG_TRANSACTION_HISTORY_ENTRY";
    private static final String ARG_INVOICE_ENTRY = "com.w1.merchant.android.activity.Details2.ARG_TRANSACTION_INVOICE_ENTRY";

    private ViewGroup mContainer;
    private ImageView mStatusIcon;
    private TextView mAmountView;

    public static void startActivity(Context context, TransactionHistoryEntry entry, View animateFrom) {
        Intent intent = new Intent(context, Details.class);
        intent.putExtra(ARG_TRANSACTION_HISTORY_ENTRY, entry);
        startActivity(context, intent, animateFrom);
    }

    public static void startActivity(Context context, Invoice entry, View animateFrom) {
        Intent intent = new Intent(context, Details.class);
        intent.putExtra(ARG_INVOICE_ENTRY, entry);
        startActivity(context, intent, animateFrom);
    }

    private static void startActivity(Context source, Intent intent, View animateFrom) {
        if (animateFrom != null && source instanceof Activity) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                    animateFrom, 0, 0, animateFrom.getWidth(), animateFrom.getHeight());
            ActivityCompat.startActivity((Activity) source, intent, options.toBundle());
        } else {
            source.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mStatusIcon = (ImageView)findViewById(R.id.status_icon);
        mAmountView = (TextView)findViewById(R.id.amount);
        mContainer = (ViewGroup)findViewById(R.id.container);
        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().hasExtra(ARG_TRANSACTION_HISTORY_ENTRY)) {
            setupTransactionHistory(getIntent().<TransactionHistoryEntry>getParcelableExtra(ARG_TRANSACTION_HISTORY_ENTRY));
        } else if (getIntent().hasExtra(ARG_INVOICE_ENTRY)) {
            setupInvoice(getIntent().<Invoice>getParcelableExtra(ARG_INVOICE_ENTRY));
        } else {
            throw new IllegalStateException();
        }
    }

    private void setupTransactionHistory(TransactionHistoryEntry entry) {
        boolean orangeColor = false;
        if (entry.isProcessed()) {
            orangeColor = true;
            mStatusIcon.setImageResource(R.drawable.ic_invoices_processing);
        } else if (entry.isAccepted()) {
            mStatusIcon.setImageResource(R.drawable.ic_invoices_accept);
        } else {
            mStatusIcon.setImageResource(R.drawable.ic_invoices_output);
        }

        boolean isFromMe = Session.getInstance().getUserId().equals(entry.fromUserId.toString());
        if (isFromMe) {
            setupAmount(entry.amount.add(entry.commissionAmount).setScale(0, RoundingMode.HALF_UP),
                    entry.currencyId, orangeColor);
        } else {
            setupAmount(entry.amount.subtract(entry.commissionAmount).setScale(0, RoundingMode.HALF_UP),
                    entry.currencyId, orangeColor);
        }

        addText(R.string.recipient_title, entry.toUserTitle);
        addNumber(R.string.from_wallet, entry.fromUserId);
        addNumber(R.string.transaction_id, entry.entryId);
        addText(R.string.sender_title, entry.fromUserTitle);
        addNumber(R.string.to_wallet, entry.toUserId);
        addAmount(R.string.commission, entry.commissionAmount.setScale(0, RoundingMode.UP), entry.currencyId);
        addText(R.string.currency, getCurrencyDescription(entry.currencyId));
        addText(R.string.description, entry.description);
        addDate(R.string.last_edit_date, entry.updateDate == null ? entry.createDate : entry.updateDate);

        String transactionStatus;
        if (entry.isAccepted()) {
            transactionStatus = entry.entryStateId + " (" + getString(R.string.paid) + ")";
        } else if (entry.isCanceled() || entry.isRejected()) {
            transactionStatus = entry.entryStateId + " (" + getString(R.string.canceled) + ")";
        } else {
            transactionStatus = entry.entryStateId + " (" + getString(R.string.processing) + ")";
        }
        addText(R.string.transaction_status, transactionStatus);

        addText(R.string.operation_id, String.valueOf(entry.operationId));

    }

    private void setupInvoice(Invoice entry) {
        boolean orangeColor = false;
        if (entry.isInProcessing()) {
            orangeColor = true;
            mStatusIcon.setImageResource(R.drawable.ic_invoices_processing);
        } else if (entry.isPaid()) {
            mStatusIcon.setImageResource(R.drawable.ic_invoices_accept);
        } else {
            mStatusIcon.setImageResource(R.drawable.ic_invoices_output);
        }

        setupAmount(entry.amount.setScale(0, BigDecimal.ROUND_HALF_UP), entry.currencyId, orangeColor);

        addNumber(R.string.invoice_title_invoice_id, entry.invoiceId);
        addNumber(R.string.invoice_title_from_user_id, entry.fromUserId);
        addNumber(R.string.invoice_title_to_user_id, entry.toUserId);
        addNumber(R.string.invoice_title_user_id, entry.userId);
        addText(R.string.invoice_title_user_title, entry.userTitle);
        addText(R.string.invoice_title_direction, entry.getLocalizedDirection(getResources()));
        addAmount(R.string.invoice_title_amount, entry.amount.setScale(0, RoundingMode.UP), entry.currencyId);
        addText(R.string.invoice_title_currency, getCurrencyDescription(entry.currencyId));
        if (!TextUtils.isEmpty(entry.orderId)) addText(R.string.invoice_title_order_id, entry.orderId);
        addText(R.string.invoice_title_description, entry.description);
        addDate(R.string.invoice_title_create_date, entry.createDate);
        addDate(R.string.invoice_title_update_date, entry.updateDate);
        addDate(R.string.invoice_title_expire_date, entry.expireDate);
        addText(R.string.invoice_title_invoice_state_id, entry.getLocalizedInvoiceState(getResources()));
        addAmount(R.string.invoice_title_paid_amount, entry.paidAmount.setScale(0, RoundingMode.HALF_UP), entry.currencyId);
        addText(R.string.invoice_title_comment, entry.comment);
        //addText(R.string.invoice_title_success_url, entry.successUrl);
        addText(R.string.invoice_title_tags, entry.tags);
    }

    private CharSequence getCurrencySymbol(String currency) {
        return TextUtilsW1.getCurrencySymbol2(currency, 2);
    }

    private CharSequence getCurrencyDescription(String currency) {
        String description = TextUtilsW1.getCurrencyName(currency, getResources());
        if (description == null) {
            return getCurrencySymbol(currency);
        } else {
            SpannableStringBuilder sb = new SpannableStringBuilder(getCurrencySymbol(currency));
            sb.append(" (");
            sb.append(description);
            sb.append(")");
            return sb;
        }
    }

    private void setupAmount(BigDecimal amount, String currency, boolean orangeColor) {
        SpannableStringBuilder builder = new SpannableStringBuilder(TextUtilsW1.formatNumber(amount));
        builder.append('\u00a0');
        builder.append(TextUtilsW1.getCurrencySymbol2(currency, 0));
        mAmountView.setText(builder);
        mAmountView.setTextColor(getResources(). getColorStateList(
                orangeColor ? R.color.details_invoice_processing : R.color.details_invoice_accepted));
    }

    private void addText(int titleResId, CharSequence value) {
        if (value == null) return;
        View root = LayoutInflater.from(this).inflate(R.layout.include_details_item, mContainer, false);
        ((TextView)root.findViewById(R.id.title)).setText(titleResId);
        ((TextView)root.findViewById(R.id.text)).setText(value);
        mContainer.addView(root);
    }

    private void addNumber(int titleResId, Number value) {
        if (value == null) return;
        addText(titleResId, value.toString());
    }

    private void addAmount(int titleResId, BigDecimal value, String currency) {
        if (value == null) return;
        SpannableStringBuilder builder = new SpannableStringBuilder(String.valueOf(value));
        builder.append('\u00a0');
        builder.append(getCurrencySymbol(currency));
        addText(titleResId, builder);
    }

    private void addDate(int titleResId, Date date){
        if (date == null) return;
        addText(titleResId,
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT).format(date));
    }
}
