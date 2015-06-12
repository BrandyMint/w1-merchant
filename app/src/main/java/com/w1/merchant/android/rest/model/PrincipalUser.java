package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by alexey on 06.02.15.
 */
public class PrincipalUser implements Parcelable {

    public String principalUserId;

    public Date createDate;

    public Date lastUsedDate;

    public String description;

    public int nativeCurrencyId = 643;

    public String title;

    public String ownerUserId;

    public String merchantLogo;

    public String merchantUrl;

    public BigDecimal nativeBalance;

    /**
     * {@linkplain Profile#ACCOUNT_TYPE_ID_PERSONAL}, {@linkplain Profile#ACCOUNT_TYPE_ID_PERSONAL}
     */
    public String accountTypeId;

    public boolean isBusinessAccount() {
        return Profile.ACCOUNT_TYPE_ID_BUSINESS.equals(accountTypeId);
    }

    public PrincipalUser() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.principalUserId);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeLong(lastUsedDate != null ? lastUsedDate.getTime() : -1);
        dest.writeString(this.description);
        dest.writeInt(this.nativeCurrencyId);
        dest.writeString(this.title);
        dest.writeString(this.ownerUserId);
        dest.writeString(this.merchantLogo);
        dest.writeString(this.merchantUrl);
        dest.writeSerializable(this.nativeBalance);
        dest.writeString(this.accountTypeId);
    }

    protected PrincipalUser(Parcel in) {
        this.principalUserId = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        long tmpLastUsedDate = in.readLong();
        this.lastUsedDate = tmpLastUsedDate == -1 ? null : new Date(tmpLastUsedDate);
        this.description = in.readString();
        this.nativeCurrencyId = in.readInt();
        this.title = in.readString();
        this.ownerUserId = in.readString();
        this.merchantLogo = in.readString();
        this.merchantUrl = in.readString();
        this.nativeBalance = (BigDecimal) in.readSerializable();
        this.accountTypeId = in.readString();
    }

    public static final Creator<PrincipalUser> CREATOR = new Creator<PrincipalUser>() {
        public PrincipalUser createFromParcel(Parcel source) {
            return new PrincipalUser(source);
        }

        public PrincipalUser[] newArray(int size) {
            return new PrincipalUser[size];
        }
    };
}
