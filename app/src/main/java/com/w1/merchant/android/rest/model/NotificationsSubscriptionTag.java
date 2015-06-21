package com.w1.merchant.android.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alexey on 21.06.15.
 */
public enum NotificationsSubscriptionTag {
    @SerializedName("account")
    ACCOUNTS("account"),

    @SerializedName("news")
    NEWS("news"),

    @SerializedName("promotions")
    PROMOTIONS("promotions"),

    UNKNOWN("unknown");

    private String id;

    private NotificationsSubscriptionTag(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static NotificationsSubscriptionTag fromId(String id) {
        for (NotificationsSubscriptionTag tag : values()) {
            if (tag.id.equals(id)) {
                return tag;
            }
        }
        return UNKNOWN;
    }
}
