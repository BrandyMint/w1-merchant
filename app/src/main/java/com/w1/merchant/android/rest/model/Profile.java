package com.w1.merchant.android.rest.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profile implements Parcelable {

    public static final String ACCOUNT_TYPE_ID_PERSONAL = "Personal";

    public static final String ACCOUNT_TYPE_ID_BUSINESS = "Business";

    public static final String MERCHANT_STATE_ACTIVE = "Active";

    public static final String MERCHANT_STATE_BLOCKED = "Blocked";

    public static final String MERCHANT_STATE_CREATED = "Created";

    public String userId;

    public boolean isOnline;

    /**
     * Тип пользователя.
     * {@linkplain #ACCOUNT_TYPE_ID_BUSINESS}, либо {@linkplain #ACCOUNT_TYPE_ID_PERSONAL}.
     */
    public String accountTypeId;

    /**
     * Состояние единой кассы. бывает
     * {@linkplain #MERCHANT_STATE_ACTIVE},
     * {@linkplain #MERCHANT_STATE_BLOCKED},
     * {@linkplain #MERCHANT_STATE_CREATED} ещё какие-то.
     */
    public String merchantStateId;

    public boolean hasContract;

    /**
     * "None", "Blocked", "Contract", "OurOffice"
     */
    public String identificationTypeId;

    public List<Attribute> userAttributes = Collections.emptyList();

    public static class Attribute implements Parcelable {

        public static final String ATTRIBUTE_TYPE_AVATAR = "Avatar";

        public static final String ATTRIBUTE_TYPE_BIRTH_DATE = "BirthDate";

        public static final String ATTRIBUTE_TYPE_BONUS_EXPIRE_DATE = "BonusExpireDate";

        public static final String ATTRIBUTE_TYPE_CARD_PAN = "CardPAN";

        public static final String ATTRIBUTE_TYPE_CULTURE = "Culture";

        public static final String ATTRIBUTE_TYPE_DESCRIPTION = "Description";

        public static final String ATTRIBUTE_TYPE_EMAIL = "Email";

        public static final String ATTRIBUTE_TYPE_FIRST_NAME = "FirstName";

        public static final String ATTRIBUTE_TYPE_GENDER = "Gender";

        public static final String ATTRIBUTE_TYPE_ICQ = "Icq";

        public static final String ATTRIBUTE_TYPE_LAST_NAME = "LastName";

        public static final String ATTRIBUTE_TYPE_LOCATION = "Location";

        public static final String ATTRIBUTE_TYPE_MERCHANT_URL = "MerchantUrl";

        public static final String ATTRIBUTE_TYPE_MERCHANT_LOGO = "MerchantLogo";

        public static final String ATTRIBUTE_TYPE_MIDDLE_NAME = "MiddleName";

        public static final String ATTRIBUTE_TYPE_TITLE = "Title";

        public static final String ATTRIBUTE_TYPE_PHONE_NUMBER = "PhoneNumber";


        public static final String VISIBILITY_TYPE_ALL = "All";

        public static final String VISIBILITY_TYPE_CONTACT = "Contact";

        public static final String VISIBILITY_TYPE_CONTACT_AND_SEARCH = "ContactAndSearch";

        public static final String VISIBILITY_TYPE_NONE = "None";


        public String userId;

        public String userAttributeId;

        public String userAttributeTypeId;

        public String visibilityTypeId;

        public boolean isReadOnly;

        public String verificationState;

        public String comment;

        public String displayValue;

        public String rawValue;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.userId);
            dest.writeString(this.userAttributeId);
            dest.writeString(this.userAttributeTypeId);
            dest.writeString(this.visibilityTypeId);
            dest.writeByte(isReadOnly ? (byte) 1 : (byte) 0);
            dest.writeString(this.verificationState);
            dest.writeString(this.comment);
            dest.writeString(this.displayValue);
            dest.writeString(this.rawValue);
        }

        public Attribute() {
        }

        protected Attribute(Parcel in) {
            this.userId = in.readString();
            this.userAttributeId = in.readString();
            this.userAttributeTypeId = in.readString();
            this.visibilityTypeId = in.readString();
            this.isReadOnly = in.readByte() != 0;
            this.verificationState = in.readString();
            this.comment = in.readString();
            this.displayValue = in.readString();
            this.rawValue = in.readString();
        }

        public static final Parcelable.Creator<Attribute> CREATOR = new Parcelable.Creator<Attribute>() {
            public Attribute createFromParcel(Parcel source) {
                return new Attribute(source);
            }

            public Attribute[] newArray(int size) {
                return new Attribute[size];
            }
        };
    }

    @Nullable
    public Attribute findAttribute(String type) {
        for (Attribute a: userAttributes) if (type.equals(a.userAttributeTypeId)) return a;
        return null;
    }

    @Nullable
    public Attribute findTitle() {
        return findAttribute(Attribute.ATTRIBUTE_TYPE_TITLE);
    }

    @Nullable
    public Attribute findMerchantUrl() {
        return findAttribute(Attribute.ATTRIBUTE_TYPE_MERCHANT_URL);
    }

    @Nullable
    public Attribute findMerchantLogo() {
        return findAttribute(Attribute.ATTRIBUTE_TYPE_MERCHANT_LOGO);
    }

    @Nullable
    public Attribute findLocation() {
        return findAttribute(Attribute.ATTRIBUTE_TYPE_LOCATION);
    }

    public boolean isBusinessAccount() {
        return ACCOUNT_TYPE_ID_BUSINESS.equals(accountTypeId);
    }

    public boolean isVerified() {
        // XXX Скорее всего, не верно
        return "None".equals(identificationTypeId)
                || "Blocked".equals(identificationTypeId);
    }

    public String getName() {
        List<String> data = new ArrayList<>(3);
        Attribute firstName = findAttribute(Attribute.ATTRIBUTE_TYPE_FIRST_NAME);
        if (firstName != null) data.add(firstName.displayValue);
        Attribute lastName = findAttribute(Attribute.ATTRIBUTE_TYPE_LAST_NAME);
        if (lastName != null) data.add(lastName.displayValue);
        Attribute middleName = findAttribute(Attribute.ATTRIBUTE_TYPE_MIDDLE_NAME);
        if (middleName != null) data.add(middleName.displayValue);

        return TextUtils.join(" ", data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeByte(isOnline ? (byte) 1 : (byte) 0);
        dest.writeString(this.accountTypeId);
        dest.writeString(this.merchantStateId);
        dest.writeByte(hasContract ? (byte) 1 : (byte) 0);
        dest.writeString(this.identificationTypeId);
        dest.writeTypedList(userAttributes);
    }

    public Profile() {
    }

    protected Profile(Parcel in) {
        this.userId = in.readString();
        this.isOnline = in.readByte() != 0;
        this.accountTypeId = in.readString();
        this.merchantStateId = in.readString();
        this.hasContract = in.readByte() != 0;
        this.identificationTypeId = in.readString();
        this.userAttributes = in.createTypedArrayList(Attribute.CREATOR);
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
