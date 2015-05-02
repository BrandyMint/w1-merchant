package com.w1.merchant.android.support;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.utils.LinkMovementMethodNoSelection;
import com.w1.merchant.android.utils.SortedList;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.TextViewImgLoader;
import com.w1.merchant.android.viewextended.CircleTransformation;
import com.w1.merchant.android.viewextended.DefaultUserpicDrawable;
import com.w1.merchant.android.viewextended.ImageLoadingGetter;
import com.w1.merchant.android.viewextended.RelativeDateTextSwitcher;

import java.util.Calendar;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolderMessage> {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    public static final int VIEW_TYPE_MY_MESSAGE = R.id.conversation_view_my_message;
    public static final int VIEW_TYPE_THEIR_MESSAGE = R.id.conversation_view_their_message;

    private final LayoutInflater mInflater;

    private final MessageFeed mMessages;

    private ImageLoadingGetter mImageGetterMyMessage;

    private ImageLoadingGetter mImageGetterTheirMessage;

    public ConversationAdapter(Context context) {
        super();
        mMessages = new MessageFeed();
        mInflater = LayoutInflater.from(context);
        //setHasStableIds(true);
    }

    @Override
    public ViewHolderMessage onCreateViewHolder(ViewGroup parent, int viewType) {
        View res;
        ViewHolderMessage holder;
        switch (viewType) {
            case VIEW_TYPE_MY_MESSAGE:
                res = mInflater.inflate(R.layout.conversation_my_message, parent, false);
                holder = new ViewHolderMyMessage(res);
                if (mImageGetterMyMessage == null) {
                    // Вычисление примерного максимального размера бабла с текстом
                    int maxBubbleTextSize;
                    int parentWidth = getParentWidth(parent);
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)holder.message.getLayoutParams();
                    maxBubbleTextSize = parentWidth - lp.leftMargin - lp.rightMargin
                            - holder.message.getPaddingLeft() - holder.message.getPaddingRight();
                    mImageGetterMyMessage = new ImageLoadingGetter(maxBubbleTextSize, parent.getContext());
                }
                break;
            case VIEW_TYPE_THEIR_MESSAGE:
                res = mInflater.inflate(R.layout.conversation_their_message, parent, false);
                holder = new ViewHolderTheirMessage(res);
                if (mImageGetterTheirMessage == null) {
                    int maxBubbleTextSize;
                    int parentWidth = getParentWidth(parent);
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)holder.message.getLayoutParams();
                    maxBubbleTextSize = parentWidth - lp.leftMargin - lp.rightMargin
                            - holder.message.getPaddingLeft() - holder.message.getPaddingRight();
                    mImageGetterTheirMessage = new ImageLoadingGetter(maxBubbleTextSize, parent.getContext());
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        return holder;
    }

    private int getParentWidth(ViewGroup parent) {
        int parentWidth = parent.getWidth();
        if (parentWidth == 0) {
            WindowManager wm = (WindowManager) parent.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)parent.getLayoutParams();
            parentWidth = size.x - lp.leftMargin - lp.rightMargin;
            if (DBG) Log.v(TAG, "display width: " + size.x);
        } else {
            if (DBG) Log.v(TAG, "parent width: " + parentWidth);
        }
        return parentWidth - parent.getPaddingLeft() - parent.getPaddingRight();
    }

    @Override
    public void onBindViewHolder(ViewHolderMessage holder, int position) {
        SupportTicketPost message = mMessages.get(position);
        bindDate(holder, message, position);
        if (message.isMyPost()) {
            bindMyMessage((ViewHolderMyMessage) holder, message);
        } else {
            bindTheirMessage((ViewHolderTheirMessage) holder, message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        SupportTicketPost message = mMessages.get(position);
        if (message.isMyPost()) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_THEIR_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public long getItemId(int position) {
        return mMessages.get(position).postId;
    }

    public void setMessages(List<SupportTicketPost> messages) {
        mMessages.resetItems(messages);
    }

    public void addMessages(List<SupportTicketPost> messages) {
        mMessages.insertItems(messages);
    }

    public void addMessage(SupportTicketPost message) {
        mMessages.insertItem(message);
    }

    public boolean isEmpty() {
        return mMessages.isEmpty();
    }

    @Nullable
    public SupportTicketPost getMessage(ViewHolderMessage holder) {
        if (holder.getPosition() == RecyclerView.NO_POSITION) return null;
        return mMessages.get(holder.getPosition());
    }

    public List<SupportTicketPost> getMessages() {
        return mMessages.getItems();
    }

    @Nullable
    public Integer findPositionById(long messageId) {
        return mMessages.findLocation(messageId);
    }

    public int getLastPosition() {
        return mMessages.size() - 1;
    }

    private void bindDate(ViewHolderMessage holder, SupportTicketPost message, int position) {
        if (dateShouldBeShown(holder, message, position)) {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setRelativeDate(message.createDate.getTime());
        } else {
            holder.date.setVisibility(View.GONE);
        }
    }

    private boolean dateShouldBeShown(ViewHolderMessage holder, SupportTicketPost message, int position) {
        if (position == 0) return true;

        Calendar curDate = Calendar.getInstance();
        Calendar predMsgDate = Calendar.getInstance();
        curDate.setTime(message.createDate);
        predMsgDate.setTime(mMessages.get(position-1).createDate);
        return !(curDate.get(Calendar.YEAR) == predMsgDate.get(Calendar.YEAR) &&
                curDate.get(Calendar.DAY_OF_YEAR) == predMsgDate.get(Calendar.DAY_OF_YEAR));
    }

    private void bindMyMessage(ViewHolderMyMessage holder, SupportTicketPost message) {
        CharSequence msg = TextUtilsW1.removeTrailingWhitespaces(message.getBodyHtml(mImageGetterMyMessage));
        msg = TextUtilsW1.replaceImgUrls(msg, mImageGetterMyMessage);
        if (DBG) Log.v(TAG, "msg: " + msg);
        holder.message.setText(msg);
        TextViewImgLoader.bindAndLoadImages(holder.message, SHOW_PHOTO_ON_CLICK_LISTENER);
    }

    private void bindTheirMessage(ViewHolderTheirMessage holder, SupportTicketPost message) {
        CharSequence msg = TextUtilsW1.removeTrailingWhitespaces(message.getBodyHtml(mImageGetterTheirMessage));
        msg = TextUtilsW1.replaceImgUrls(msg, mImageGetterTheirMessage);
        holder.message.setText(msg);
        holder.username.setText(message.userTitle);
        bindAvatar(holder, message);
        TextViewImgLoader.bindAndLoadImages(holder.message, SHOW_PHOTO_ON_CLICK_LISTENER);
    }

    private void bindAvatar(ViewHolderTheirMessage holder, SupportTicketPost message) {
        DefaultUserpicDrawable defaultUserpicDrawable;
        String avatarUri;
        int avatarDiameter;

        avatarDiameter = holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_small_diameter);
        avatarUri = message.getAvatarUri(avatarDiameter);
        if (holder.avatarUri != null && holder.avatarUri.equals(avatarUri)) return;

        defaultUserpicDrawable = new DefaultUserpicDrawable();
        defaultUserpicDrawable.setBounds(0, 0, avatarDiameter, avatarDiameter);
        defaultUserpicDrawable.setUser(message.email);

        holder.avatarUri = avatarUri;
        if (avatarUri != null) {
            Picasso.with(holder.itemView.getContext())
                    .load(message.getAvatarUri(avatarDiameter))
                    .placeholder(R.drawable.avatar_dummy)
                    .error(defaultUserpicDrawable)
                    .transform(CircleTransformation.getInstance())
                    .into(holder.avatar);
        } else {
            Picasso.with(holder.itemView.getContext()).cancelRequest(holder.avatar);
            holder.avatar.setImageDrawable(defaultUserpicDrawable);
        }
    }

    private final class MessageFeed extends SortedList<SupportTicketPost> implements SortedList.OnListChangedListener {

        public MessageFeed() {
            super(SupportTicketPost.SORT_BY_DATE_ID_COMPARATOR);
            setListener(this);
        }

        @Override
        public long getItemId(SupportTicketPost item) {
            return item.postId;
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

    public abstract static class ViewHolderMessage extends RecyclerView.ViewHolder {
        public final RelativeDateTextSwitcher date;
        public final TextView message;
        public final TextView username;

        public ViewHolderMessage(View root) {
            super(root);
            message = (TextView)root.findViewById(R.id.message);
            username = (TextView)root.findViewById(R.id.username);
            date = (RelativeDateTextSwitcher)root.findViewById(R.id.relative_date);
            message.setMovementMethod(LinkMovementMethodNoSelection.getInstance());
        }

    }

    public static class ViewHolderMyMessage extends ViewHolderMessage {
        private ViewHolderMyMessage(View v) {
            super(v);
        }
    }

    public static class ViewHolderTheirMessage extends ViewHolderMessage {

        public final ImageView avatar;

        public String avatarUri;

        private ViewHolderTheirMessage(View v) {
            super(v);
            this.avatar = (ImageView)v.findViewById(R.id.avatar);
        }
    }

    private static final TextViewImgLoader.OnClickListener SHOW_PHOTO_ON_CLICK_LISTENER = new TextViewImgLoader.OnClickListener() {
        @Override
        public void onImageClicked(TextView widget, String source) {
            int width = widget.getWidth() - widget.getPaddingLeft() - widget.getPaddingRight();
            ShowPhotoActivity.startShowPhotoActivity(widget.getContext(), source, width, widget);
        }
    };
}