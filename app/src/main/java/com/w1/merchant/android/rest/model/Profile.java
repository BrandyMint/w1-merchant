package com.w1.merchant.android.rest.model;


import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profile {

    public String userId;

    public boolean isOnline;

    public String accountTypeId;

    public String merchantStateId;

    public List<Attribute> userAttributes = Collections.emptyList();

    public static class Attribute {

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

}
