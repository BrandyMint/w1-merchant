package com.w1.merchant.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Invoice;
import com.w1.merchant.android.utils.SortedList;
import com.w1.merchant.android.utils.TextUtilsW1;

import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class InvoiceListAdapter extends BaseAdapter {

    private final SortedList<Invoice> mList;
    private final LayoutInflater mInflater;

    public InvoiceListAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(context);
        mList = new SortedList<Invoice>(Invoice.SORT_BY_DATE_DESC_DESC_ID_COMPARATOR) {
            @Override
            public long getItemId(Invoice item) {
                return item.invoiceId.longValue();
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
    public Invoice getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).invoiceId.longValue();
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
            root.setTag(R.id.tag_invoice_view_holder, holder);
        } else {
            root = convertView;
            holder = (ViewHolder) root.getTag(R.id.tag_invoice_view_holder);
        }

        bindView(position, holder);

        return root;
    }

    public void setItems(List<Invoice> entries) {
        mList.resetItems(entries);
    }

    public void addItems(List<Invoice> entries) {
        mList.insertItems(entries);
    }

    void bindView(int position, ViewHolder holder) {
        Invoice entry = getItem(position);
        bindIcon(entry, holder);
        bindName(entry, holder);
        bindDate(entry, holder);
        bindAmountCurrency(entry, holder);
    }

    private void bindName(Invoice entry, ViewHolder holder) {
        holder.name.setText(entry.invoiceId.toString());
    }

    private void bindDate(Invoice entry, ViewHolder holder) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(entry.createDate);
        holder.date.setText(TextUtilsW1.dateFormat(cal, holder.root.getResources()));
    }

    private void bindAmountCurrency(Invoice entry, ViewHolder holder) {
        int textColor;
        if (Invoice.STATE_ACCEPTED.equals(entry.invoiceStateId)) {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_plus);
        } else if (Invoice.STATE_CANCELED.equals(entry.invoiceStateId)
                || Invoice.STATE_EXPIRED.equals(entry.invoiceStateId)
                || Invoice.STATE_REJECTED.equals(entry.invoiceStateId)) {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_minus);
        } else {
            textColor = holder.amount.getResources().getColor(R.color.amount_color_zero);
        }

        holder.amount.setTextColor(textColor);
        holder.amount.setText(TextUtilsW1.formatAmount(entry.amount.setScale(0, RoundingMode.UP),
                entry.currencyId));
    }

    private void bindIcon(Invoice entry, ViewHolder holder) {
        // TODO suspense
        if (Invoice.STATE_ACCEPTED.equals(entry.invoiceStateId)) {
            holder.icon.setImageResource(R.drawable.icon_ok);
        } else if (Invoice.STATE_CANCELED.equals(entry.invoiceStateId)
                || Invoice.STATE_EXPIRED.equals(entry.invoiceStateId)
                || Invoice.STATE_REJECTED.equals(entry.invoiceStateId)) {
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
