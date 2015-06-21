package com.w1.merchant.android.rest.model;

import com.google.gson.annotations.SerializedName;
import com.w1.merchant.android.utils.NetworkUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationsSubscriptionRequest {

    @SerializedName("os")
    public final String operatingSystem = "android";

    public String id;

    public String culture;

    public String uri;

    public List<NotificationsSubscriptionTag> tags = Collections.emptyList();

    public NotificationsSubscriptionRequest createSubscribeRequest(String deviceId, String gcmRegId, NotificationsSubscriptionTag... tags) {
        NotificationsSubscriptionRequest req = new NotificationsSubscriptionRequest();
        req.id = deviceId;
        req.culture = getLangTag();
        req.uri = uri;
        req.tags = Arrays.asList(tags);

        return req;
    }

    private static String getLangTag() {
        String culture = NetworkUtils.getLangTag();
        if ("ru".equalsIgnoreCase(culture) || "ru-ru".equalsIgnoreCase(culture)) {
            return "ru-ru";
        } else {
            return "en-us";
        }
    }
}
