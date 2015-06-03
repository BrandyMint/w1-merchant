package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by alexey on 06.02.15.
 */
public class PrincipalUser implements Parcelable {

    public String principalUserId;

    public Date createDate;

    public String description;

    public int nativeCurrencyId = 643;

    public String title;

    public String ownerUserId;

    public String merchantLogo;

    public String merchantUrl;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.principalUserId);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeString(this.description);
        dest.writeInt(this.nativeCurrencyId);
        dest.writeString(this.title);
        dest.writeString(this.ownerUserId);
        dest.writeString(this.merchantLogo);
        dest.writeString(this.merchantUrl);
    }

    public PrincipalUser() {
    }

    private PrincipalUser(Parcel in) {
        this.principalUserId = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        this.description = in.readString();
        this.nativeCurrencyId = in.readInt();
        this.title = in.readString();
        this.ownerUserId = in.readString();
        this.merchantLogo = in.readString();
        this.merchantUrl = in.readString();
    }

    public static final Parcelable.Creator<PrincipalUser> CREATOR = new Parcelable.Creator<PrincipalUser>() {
        public PrincipalUser createFromParcel(Parcel source) {
            return new PrincipalUser(source);
        }

        public PrincipalUser[] newArray(int size) {
            return new PrincipalUser[size];
        }
    };
}
