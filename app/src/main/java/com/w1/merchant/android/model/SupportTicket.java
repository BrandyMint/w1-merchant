package com.w1.merchant.android.model;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.utils.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportTicket implements Parcelable {

    public static final String STATUS_OPEN = "4";

    public static final String STATUS_IN_PROGRESS = "5";

    public static final String STATUS_CLOSED = "6";

    public static final String STATUS_WAITING_ON_THIRD_PARTY = "7";

    public static final String STATUS_CHECKING = "8";

    public static final String STATUS_RESOLVED = "9";

    /**
     * Сортировка по дате модификации / создания, затем - по id
     */
    public static Comparator<SupportTicket> SORT_BY_DATE_DESC_DESC_ID_COMPARATOR = new Comparator<SupportTicket>() {
        @Override
        public int compare(SupportTicket lhs, SupportTicket rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            } else if (lhs == null) {
                return -1;
            } else if (rhs == null) {
                return 1;
            } else {
                long lhsDate, rhsDate;

                lhsDate = lhs.getUpdateOrCreateDate() != null ? lhs.getUpdateOrCreateDate().getTime() : 0;
                rhsDate = rhs.getUpdateOrCreateDate() != null ? rhs.getUpdateOrCreateDate().getTime() : 0;

                int compareDates = Utils.compare(rhsDate, lhsDate);
                return compareDates != 0 ? compareDates : Utils.compare(lhs.ticketId, rhs.ticketId);
            }
        }
    };

    public long ticketId;

    public String ticketMask;

    public String statusId;

    public String userId;

    public int repliesCount;

    public String subject;

    public Date createDate;

    @Nullable
    public Date updateDate;

    @Nullable
    public Date lastReplyDate;

    public List<SupportTicketPost> posts = Collections.emptyList();

    public int postsCount;

    @Nullable
    public SupportTicketPost getLastMessage() {
        return posts.isEmpty() ? null : posts.get(posts.size()-1);
    }

    public boolean isOpen() {
        return STATUS_OPEN.equals(statusId);
    }

    public boolean isClosed() {
        return STATUS_CLOSED.equals(statusId) || STATUS_RESOLVED.equals(statusId);
    }

    public Date getUpdateOrCreateDate() {
        return updateDate != null ? updateDate : createDate;
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equals(statusId);
    }

    public boolean isInProgress() {
        return !isClosed() && !isOpen();
    }

    public static class CreateRequest {

        final String email;

        final String subject;

        final String body;

        public CreateRequest(String subject, String body, Resources resources) {
            this.email = getSupportEmail(resources);
            this.subject = subject == null ? "" : subject;
            this.body = body == null ? "" : body;
        }

        private String getSupportEmail(Resources resources) {
            try {
                switch (resources.getConfiguration().locale.getISO3Language().toLowerCase(Locale.US)) {
                    case "ua":
                    case "uah":
                        return Constants.SUPPORT_EMAIL_UAH;
                    case "za":
                    case "zar":
                        return Constants.SUPPORT_EMAIL_ZAR;
                    default:
                        return Constants.SUPPORT_EMAIL_MAIN;
                }
            } catch (Throwable e) {
                return Constants.SUPPORT_EMAIL_MAIN;
            }
        }
    }

    public static class ReplyRequest {
        final String body;

        public ReplyRequest(String body) {
            this.body = body;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.ticketId);
        dest.writeString(this.ticketMask);
        dest.writeString(this.statusId);
        dest.writeString(this.userId);
        dest.writeInt(this.repliesCount);
        dest.writeString(this.subject);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeLong(updateDate != null ? updateDate.getTime() : -1);
        dest.writeLong(lastReplyDate != null ? lastReplyDate.getTime() : -1);
        dest.writeTypedList(posts);
        dest.writeInt(this.postsCount);
    }

    public SupportTicket() {
    }

    private SupportTicket(Parcel in) {
        this.ticketId = in.readLong();
        this.ticketMask = in.readString();
        this.statusId = in.readString();
        this.userId = in.readString();
        this.repliesCount = in.readInt();
        this.subject = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        long tmpUpdateDate = in.readLong();
        this.updateDate = tmpUpdateDate == -1 ? null : new Date(tmpUpdateDate);
        long tmpLastReplyDate = in.readLong();
        this.lastReplyDate = tmpLastReplyDate == -1 ? null : new Date(tmpLastReplyDate);
        posts = in.createTypedArrayList(SupportTicketPost.CREATOR);
        this.postsCount = in.readInt();
    }

    public static final Parcelable.Creator<SupportTicket> CREATOR = new Parcelable.Creator<SupportTicket>() {
        public SupportTicket createFromParcel(Parcel source) {
            return new SupportTicket(source);
        }

        public SupportTicket[] newArray(int size) {
            return new SupportTicket[size];
        }
    };
}
