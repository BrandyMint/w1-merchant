package com.w1.merchant.android.ui.adapter;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.Provider;
import com.w1.merchant.android.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class WithdrawalGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_LOADING_INDICATOR = R.id.view_type_loading_indicator;
    public static final int VIEW_TYPE_GRID_ITEM = R.id.view_type_list_item;

    private final android.support.v7.util.SortedList<Provider> mList;

    private final Picasso mPicasso;

    private boolean mShowIsLoading = false;

    private final NumberFormat mCommissionFormat;

    @MainThread
    public WithdrawalGridAdapter(final Context context) {
        mPicasso = Picasso.with(context);
        mCommissionFormat = DecimalFormat.getPercentInstance(Locale.getDefault());
        mCommissionFormat.setMaximumFractionDigits(2);

        mList = new android.support.v7.util.SortedList<>(Provider.class, new android.support.v7.util.SortedList.Callback<Provider>() {

            private final Provider.SortByTitleComparator mComparator = new Provider.SortByTitleComparator();

            @Override
            public int compare(Provider o1, Provider o2) {
                return mComparator.compare(o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Provider oldItem, Provider newItem) {
                return TextUtils.equals(oldItem.providerId, newItem.providerId)
                        && TextUtils.equals(oldItem.logoUrl, newItem.logoUrl)
                        && TextUtils.equals(oldItem.title, newItem.title)
                        && TextUtils.equals(oldItem.description, newItem.description)
                        && TextUtils.equals(oldItem.currencyId, newItem.currencyId)
                        && Utils.equals(oldItem.commission.rate, newItem.commission.rate)
                        ;
            }

            @Override
            public boolean areItemsTheSame(Provider item1, Provider item2) {
                return TextUtils.equals(item1.providerId, item2.providerId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size() + (mShowIsLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPendingIndicatorPosition(position)) {
            return VIEW_TYPE_LOADING_INDICATOR;
        } else {
            return VIEW_TYPE_GRID_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_LOADING_INDICATOR:
                root = inflater.inflate(R.layout.item_loading_indicator, parent, false);
                return new ViewHolderLoadingIndicator(root);
            case VIEW_TYPE_GRID_ITEM:
                root = inflater.inflate(R.layout.item_provider_grid_cell, parent, false);
                return new ViewHolderItem(root);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof  ViewHolderItem) {
            ViewHolderItem vh = (ViewHolderItem)holder;
            Provider provider = mList.get(position);
            bindLogo(vh, provider);
            bindCommission(vh, provider);
            bindTitle(vh, provider);
            bindDescription(vh, provider);
        }
    }

    @MainThread
    public void setShowLoadMoreButton(boolean show) {
        if (show == mShowIsLoading) return;
        mShowIsLoading = show;
        if (mShowIsLoading) {
            notifyItemInserted(getPendingIndicatorPosition());
        } else {
            notifyItemRemoved(getPendingIndicatorPosition());
        }
    }

    @MainThread
    public int getPendingIndicatorPosition() {
        return mList.size();
    }

    @MainThread
    public boolean isPendingIndicatorPosition(int position) {
        return mShowIsLoading && position == getPendingIndicatorPosition();
    }

    public android.support.v7.util.SortedList<Provider> getList() {
        return mList;
    }

    private void bindLogo(ViewHolderItem holder, Provider provider) {
        mPicasso
                .load(provider.getLogoUrl())
                .into(holder.logo);

    }

    private void bindCommission(ViewHolderItem holder, Provider provider) {
        String rate = mCommissionFormat.format(provider.commission.rate.divide(BigDecimal.valueOf(100),
                4, RoundingMode.UP));
        if (provider.commission.hasAdditionalCost()) rate += "+";
        rate = rate.replaceAll("\\s+", "");
        holder.commission.setText(rate);
    }

    private void bindTitle(ViewHolderItem holder, Provider provider) {
        holder.title.setText(provider.title);
    }

    private void bindDescription(ViewHolderItem holder, Provider provider) {
        holder.itemView.setContentDescription(provider.description);
    }

    public static class ViewHolderLoadingIndicator extends RecyclerView.ViewHolder {
        public ViewHolderLoadingIndicator(View itemView) {
            super(itemView);
        }
    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder {

        public final ImageView logo;

        public final TextView commission;

        public final TextView title;

        public ViewHolderItem(View root) {
            super(root);
            logo = (ImageView)root.findViewById(R.id.logo);
            commission = (TextView)root.findViewById(R.id.commission);
            title = (TextView)root.findViewById(R.id.title);
        }
    }

}
