package com.w1.merchant.android.model;


import android.support.annotation.Nullable;

public class OneTimePassword {

    public static final class Request {
        public final String login;

        public final Integer expiredInMinutes;

        public Request(String login, @Nullable Integer expired) {
            this.login = login;
            this.expiredInMinutes = expired;
        }

        public Request(String login) {
            this(login, null);
        }

    }
}
