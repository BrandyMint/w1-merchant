package com.w1.merchant.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.rest.model.TransactionHistoryEntry;
import com.w1.merchant.android.utils.SortedList;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Выписка, последние операции
 */
public class TransactionHistoryAdapter extends BaseAdapter {

    private final SortedList<TransactionHistoryEntry> mList;
    private final LayoutInflater mInflater;

    public TransactionHistoryAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(context);
        mList = new SortedList<TransactionHistoryEntry>(TransactionHistoryEntry.SORT_BY_DATE_DESC_DESC_ID_COMPARATOR) {
            @Override
            public long getItemId(TransactionHistoryEntry item) {
                return item.entryId.longValue();
            }
        };
        mList.setListener(new SortedList.OnListChangedListener() {
            @Override
            public void onDataSetChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemChanged(int location) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemInserted(int location) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRemoved(int location) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemMoved(int fromLocation, int toLocation) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int locationStart, int itemCount) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int locationStart, int itemCount) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(int locationStart, int itemCount) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public TransactionHistoryEntry getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).entryId.longValue();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View root;
        final ViewHolder holder;
        if (convertView == null) {
            root = mInflater.inflate(R.layout.transaction_history_entry, parent, false);
            holder = new ViewHolder(root);
            root.setTag(R.id.tag_transaction_history_view_holder, holder);
        } else {
            root = convertView;
            holder = (ViewHolder) root.getTag(R.id.tag_transaction_history_view_holder);
        }

        bindView(position, holder);

        return root;
    }

    public void setItems(List<TransactionHistoryEntry> entries) {
        mList.resetItems(entries);
    }

    public void addItems(List<TransactionHistoryEntry> entries) {
        mList.insertItems(entries);
    }

    void bindView(int position, ViewHolder holder) {
        TransactionHistoryEntry entry = getItem(position);
        bindIcon(entry, holder);
        bindName(entry, holder);
        bindDate(entry, holder);
        bindAmountCurrency(entry, holder);
    }

    private void bindName(TransactionHistoryEntry entry, ViewHolder holder) {
        String descrOnlyDigits = entry.description.replaceAll("[^0-9]", "");
        if (entry.isTypeProviderPayment()) {
            holder.name.setText(R.string.output_cash);
        } else if (descrOnlyDigits.isEmpty()) {
            holder.name.setText(R.string.output_cash);
        } else {
            if (descrOnlyDigits.length() > 12) {
                holder.name.setText(descrOnlyDigits.substring(descrOnlyDigits.length() - 12));
            } else {
                holder.name.setText(descrOnlyDigits);
            }
        }
    }

    private void bindDate(TransactionHistoryEntry entry, ViewHolder holder) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(entry.createDate);
        holder.date.setText(TextUtilsW1.dateFormat(cal, holder.root.getResources()));
    }

    private void bindAmountCurrency(TransactionHistoryEntry entry, ViewHolder holder) {
        String res;
        BigDecimal amount0;

        boolean isFromMe = Session.getInstance().getUserId().equals(entry.fromUserId.toString());
        if (isFromMe) {
            amount0 = entry.amount.add(entry.commissionAmount).setScale(0, RoundingMode.UP);
        } else {
            amount0 = entry.amount.subtract(entry.commissionAmount).setScale(0, RoundingMode.UP);
        }

        int textColor;
        if (isFromMe) {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_minus);
        } else if (entry.isProcessed()) {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_zero);
        } else {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_plus);
        }

        holder.amount.setTextColor(textColor);
        holder.amount.setText(TextUtilsW1.formatAmount(amount0, entry.currencyId));
    }

    private void bindIcon(TransactionHistoryEntry entry, ViewHolder holder) {
        if (entry.isAccepted()) {
            holder.icon.setImageResource(R.drawable.icon_ok);
        } else if (entry.isCanceled() || entry.isRejected()) {
            holder.icon.setImageResource(R.drawable.icon_cancel);
        } else {
            holder.icon.setImageResource(R.drawable.icon_progress);
        }
    }

    public static class ViewHolder {
        public final View root;
        public final ImageView icon;
        public final TextView amount;
        public final TextView date;
        public final TextView name;

        public ViewHolder(View root) {
            this.root = root;
            icon = (ImageView)root.findViewById(R.id.icon);
            amount = (TextView)root.findViewById(R.id.amount);
            date = (TextView)root.findViewById(R.id.date);
            name = (TextView)root.findViewById(R.id.name);
        }
    }
}
