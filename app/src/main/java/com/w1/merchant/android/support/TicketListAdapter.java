package com.w1.merchant.android.support;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.utils.SortedList;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.viewextended.CircleTransformation;
import com.w1.merchant.android.viewextended.DefaultUserpicDrawable;
import com.w1.merchant.android.viewextended.SmartTextSwitcher;

import java.util.ArrayList;
import java.util.List;

public abstract class TicketListAdapter extends RecyclerView.Adapter<TicketListAdapter.ViewHolder> {

    private TicketList mList;

    public abstract void initClickListeners(ViewHolder holder);

    public TicketListAdapter() {
        super();
        mList = new TicketList();
        setHasStableIds(true);
    }

    @Override
    public TicketListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ticket_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(root);
        holder.lastMessage.setMaxLines(5); // Требуется здесь, а не в xml, чтобы инициализировался EllipsizingTextView
        initClickListeners(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(TicketListAdapter.ViewHolder viewHolder, int position) {
        SupportTicket ticket = mList.get(position);
        bindReadStatus(viewHolder, ticket);
        bindAvatar(viewHolder, ticket);
        bindText(viewHolder, ticket);
        bindDate(viewHolder, ticket);
        bindUnreadMessages(viewHolder, ticket);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.itemView.getContext() != null) {
            Picasso.with(holder.itemView.getContext()).cancelRequest(holder.avatar);
        }
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).ticketId;
    }

    public SupportTicket getItem(int position) {
        return mList.get(position);
    }

    public boolean isEmpty() {
        return mList.isEmpty();
    }

    public void setTickets(List<SupportTicket> tickets) {
        mList.resetItems(tickets);
    }

    public void addTicket(SupportTicket ticket) {
        mList.insertItem(ticket);
    }

    public ArrayList<SupportTicket> getTickets() {
        return new ArrayList<>(mList.getItems());
    }

    public void refreshRelativeDates(RecyclerView recyclerView) {
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder != null && holder instanceof ViewHolder) {
                ViewHolder ticketHolder = (ViewHolder) holder;
                CharSequence newDate = getRelativeDate(recyclerView.getContext(), ticketHolder.lastDateValue);
                if (!newDate.toString().equals(ticketHolder.date.getText().toString()))
                    notifyItemChanged(holder.getPosition());
            }
        }
    }


    private void bindReadStatus(ViewHolder holder, SupportTicket ticket) {
        // TODO Определять не по закрыти, а по прочитано/не прочитано?
        Resources resources = holder.itemView.getResources();
        if (ticket.isClosed()) {
            holder.lastMessage.setTextColor(resources.getColorStateList(R.color.ticket_list_read_message));
            holder.date.setTextColor(resources.getColorStateList(R.color.ticket_list_read_message_date));
        } else {
            holder.lastMessage.setTextColor(resources.getColorStateList(R.color.ticket_list_unread_message));
            holder.date.setTextColor(resources.getColorStateList(R.color.ticket_list_unread_message_date));
        }
    }

    private void bindText(ViewHolder holder, SupportTicket ticket) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SupportTicketPost lastMessage = ticket.getLastMessage();
        if (!TextUtilsW1.isBlank(ticket.subject)) {
            String subject = TextUtilsW1.removeTrailingWhitespaces(ticket.subject).toString();
            builder.append(TextUtilsW1.removeTrailingWhitespaces(ticket.subject));
            TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(holder.itemView.getContext(),
                    R.style.TicketListSubjectTextAppearance);
            builder.setSpan(textAppearanceSpan, 0, ticket.subject.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if (lastMessage != null && !TextUtilsW1.isBlank(lastMessage.body)) {
            builder.append("\n");
            builder.append(TextUtilsW1.removeTrailingWhitespaces(lastMessage.getBodyHtml(null)));
        }

        holder.lastMessage.setText(builder);
    }

    private void bindDate(ViewHolder holder, SupportTicket ticket) {
        long msgTime;

        if (ticket.lastReplyDate != null) {
            msgTime = ticket.lastReplyDate.getTime();
        } else if(ticket.updateDate != null) {
            msgTime = ticket.updateDate.getTime();
        } else {
            msgTime = ticket.createDate.getTime();
        }

        // Коррекция на разницу часов серверных и устройства
        long now = System.currentTimeMillis();
        if (now < msgTime) msgTime = now;

        holder.lastDateValue = msgTime;
        CharSequence newDate = getRelativeDate(holder.itemView.getContext(), msgTime);
        if (holder.lastDateTicketId != ticket.ticketId) {
            holder.date.setCurrentText(newDate); // Не анимируем при бинде нового тикета в имеющийся view.
            holder.lastDateTicketId = ticket.ticketId;
        } else {
            holder.date.setText(newDate);
        }
    }

    private CharSequence getRelativeDate(Context context, long msgTime) {
        return DateUtils.getRelativeDateTimeString(context, msgTime,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_MONTH);
    }

    private void bindUnreadMessages(ViewHolder holder, SupportTicket ticket) {
        if (ticket.repliesCount > 0) {
            holder.repliesCount.setVisibility(View.VISIBLE);
            holder.repliesCount.setText(String.valueOf(ticket.postsCount));
        } else {
            holder.repliesCount.setVisibility(View.INVISIBLE);
        }
    }

    private void bindAvatar(ViewHolder holder, SupportTicket ticket) {
        DefaultUserpicDrawable defaultUserpicDrawable;
        String avatarUri;
        int avatarDiameter;

        avatarDiameter = holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_small_diameter);
        defaultUserpicDrawable = new DefaultUserpicDrawable();
        defaultUserpicDrawable.setBounds(0, 0, avatarDiameter, avatarDiameter);

        if (ticket.getLastMessage() != null) {
            avatarUri = ticket.getLastMessage().getAvatarUri(avatarDiameter);
            defaultUserpicDrawable.setUser(ticket.getLastMessage().email);
        } else {
            avatarUri = null;
        }

        if (holder.avatarImageUri != null && holder.avatarImageUri.equals(avatarUri)) {
            return;
        }
        holder.avatarImageUri = avatarUri;

        if (avatarUri != null) {
            Picasso.with(holder.itemView.getContext())
                    .load(avatarUri)
                    .placeholder(R.drawable.avatar_dummy)
                    .error(defaultUserpicDrawable)
                    .transform(CircleTransformation.getInstance())
                    .into(holder.avatar);
        } else {
            Picasso.with(holder.itemView.getContext()).cancelRequest(holder.avatar);
            holder.avatar.setImageDrawable(defaultUserpicDrawable);
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView avatar;
        public final TextView lastMessage;
        public final SmartTextSwitcher date;
        public final TextView repliesCount;

        public String avatarImageUri;
        public long lastDateTicketId;
        public long lastDateValue;

        public ViewHolder(View v) {
            super(v);
            avatar = (ImageView)v.findViewById(R.id.avatar);
            lastMessage = (TextView)v.findViewById(R.id.last_message);
            date = (SmartTextSwitcher)v.findViewById(R.id.notification_date);
            repliesCount = (TextView)v.findViewById(R.id.replies_count);
        }
    }

    private final class TicketList extends SortedList<SupportTicket> implements SortedList.OnListChangedListener {

        public TicketList() {
            super(SupportTicket.SORT_BY_DATE_DESC_DESC_ID_COMPARATOR);
            setListener(this);
        }

        @Override
        public long getItemId(SupportTicket item) {
            return item.ticketId;
        }

        @Override
        public void onDataSetChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemChanged(int location) {
            notifyItemChanged(location);
        }

        @Override
        public void onItemInserted(int location) {
            notifyItemInserted(location);
        }

        @Override
        public void onItemRemoved(int location) {
            notifyItemRemoved(location);
        }

        @Override
        public void onItemMoved(int fromLocation, int toLocation) {
            notifyItemMoved(fromLocation, toLocation);
        }

        @Override
        public void onItemRangeChanged(int locationStart, int itemCount) {
            notifyItemRangeChanged(locationStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int locationStart, int itemCount) {
            notifyItemRangeInserted(locationStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int locationStart, int itemCount) {
            notifyItemMoved(locationStart, itemCount);
        }
    }

}
