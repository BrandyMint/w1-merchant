package com.w1.merchant.android.rest.model;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Html;

import com.w1.merchant.android.Constants;
import com.w1.merchant.android.R;
import com.w1.merchant.android.utils.TextUtilsW1;
import com.w1.merchant.android.utils.Utils;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class SupportTicketPost implements Parcelable {

    public static Comparator<SupportTicketPost> SORT_BY_DATE_ID_COMPARATOR = new Comparator<SupportTicketPost>() {
        @Override
        public int compare(SupportTicketPost lhs, SupportTicketPost rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            } else if (lhs == null) {
                return -1;
            } else if (rhs == null) {
                return 1;
            } else {
                long lhsDate = lhs.createDate != null ? lhs.createDate.getTime() : 0;
                long rhsDate = rhs.createDate != null ? rhs.createDate.getTime() : 0;
                int compareDates = Utils.compare(lhsDate, rhsDate);
                return compareDates != 0 ? compareDates : Utils.compare(lhs.postId, rhs.postId);
            }
        }
    };

    public long postId;

    public long ticketId;

    public long userId;

    public String userTitle;

    public Date createDate;

    public String body;

    public String email;

    private transient volatile String mEmailMd5Cached;

    public static SupportTicketPost createMayIHelpYouFakePost(Resources resources) {
        SupportTicketPost post = new SupportTicketPost();
        post.postId = -1;
        post.ticketId = -1;
        post.userId = 0;
        post.userTitle = "";
        post.createDate = new Date();
        post.body = resources.getString(R.string.hello_how_can_we_help_you_message);
        post.email = Constants.SUPPORT_EMAIL_MAIN;
        return post;
    }

    public boolean isMyPost() {
        return userId != 0; // TODO
    }

    @Nullable
    public String getAvatarUri(int size) {
        if (email == null || email.trim().isEmpty()) return null;
        if (size == 0) size = 72;
        return "https://gravatar.com/avatar/" + getEmailMd5() + "?s="+size+"&d=404";
    }

    private String getEmailMd5() {
        if (mEmailMd5Cached == null) {
            synchronized (this) {
                if (mEmailMd5Cached == null) {
                    mEmailMd5Cached = Utils.md5Hex(email.toLowerCase(Locale.US).trim());
                }
                return mEmailMd5Cached;
            }
        }
        return mEmailMd5Cached;
    }

    public CharSequence getBodyHtml(Html.ImageGetter imageGetter) {
        return TextUtilsW1.safeFromHtmlPreLine(body, imageGetter);
    }

    public SupportTicketPost() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.postId);
        dest.writeLong(this.ticketId);
        dest.writeLong(this.userId);
        dest.writeString(this.userTitle);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeString(this.body);
        dest.writeString(this.email);
    }

    private SupportTicketPost(Parcel in) {
        this.postId = in.readLong();
        this.ticketId = in.readLong();
        this.userId = in.readLong();
        this.userTitle = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        this.body = in.readString();
        this.email = in.readString();
    }

    public static final Creator<SupportTicketPost> CREATOR = new Creator<SupportTicketPost>() {
        public SupportTicketPost createFromParcel(Parcel source) {
            return new SupportTicketPost(source);
        }

        public SupportTicketPost[] newArray(int size) {
            return new SupportTicketPost[size];
        }
    };
}
