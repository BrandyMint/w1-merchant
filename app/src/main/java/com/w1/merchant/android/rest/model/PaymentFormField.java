package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.w1.merchant.android.utils.Utils;

import java.util.Comparator;
import java.util.List;

/**
 * Поле в платежной форме
 */
public class PaymentFormField implements Parcelable {

    public static final Comparator<PaymentFormField> SORT_BY_TAB_ORDER_COMPARATOR = new Comparator<PaymentFormField>() {
        @Override
        public int compare(PaymentFormField lhs, PaymentFormField rhs) {
            if (lhs == rhs) {
                return 0;
            } else if (lhs == null) {
                return -1;
            } else if (rhs == null) {
                return 1;
            } else {
                int tabOrder = Utils.compare(lhs.tabOrder, rhs.tabOrder);
                return tabOrder != 0 ? tabOrder : Utils.compare(lhs.fieldId, rhs.fieldId);
            }
        }
    };

    /**
     * Скалярные. поля, значения которых можно описать одним числом
     * или строкой (номер договора, номер счета, телефона, примечание, ФИО и т.п.);
     */
    public static final String FIELD_TYPE_SCALAR = "Scalar";

    /**
     * Списковые. поля, значения которых необходимо выбрать из предопределённого
     * списка. В качестве компонента пользовательского интерфейса рекомендуется
     * использовать выпадающий список ListBox;
     */
    public static final String FIELD_TYPE_LIST = "List";

    /**
     * Нередактируемые.  поля, значение которых задано сервисом и не заполняется
     * пользователем. В качестве компонента пользователького интерфейса
     * рекомендуется использовать Label.
     */
    public static final String FIELD_TYPE_LABEL = "Label";

    /**
     * Тип поля
     */
    public String fieldType = FIELD_TYPE_SCALAR;


    /**
     * Идентификатор поля
     */
    public String fieldId;

    /**
     * Название поля
     */
    public String title;

    /**
     * Описание поля
     */
    public String description;

    /**
     * Позиция поля в форме
     */
    public int tabOrder;

    /**
     * Флаг, указывающий, является ли поле обязательным.
     */
    public boolean isRequired = false;


    /*********
     *
     *  Scalar
     *
     */

    /**
     * Значение поля по умолчанию
     */
    public String defaultValue;

    /**
     * Пример значения поля
     */
    public String example;

    /**
     * Регулярное выражение, которому должно удовлетворять значение поля
     */
    public String regEx;

    /**
     * Минимальная длина значения
     */
    public int minLength;

    /**
     * Максимальная длина значения
     */
    public int maxLength;


    /*******
     *
     * List
     *
     */

    /**
     * Минимальное число выбранных элементов.
     */
    // minLength

    /**
     * Максимальное число выбранных элементов.
     */
    // maxLength

    /**
     * Элементы списка
     */
    public List<PaymentFormListFieldItem> items;


    /********
     * Value
     */

    public String value;

    public boolean isAmountField() {
        return FIELD_TYPE_SCALAR.equals(fieldType) && "Amount".equalsIgnoreCase(fieldId);
    }

    /**
     * @return Первый выбранный элемент в списковом поле
     */
    @Nullable
    public PaymentFormListFieldItem getSelectedItem() {
        for (PaymentFormListFieldItem item : items) {
            if (item.isSelected) return item;
        }
        return null;
    }

    @Nullable
    public PaymentFormListFieldItem findListItem(String value) {
        for (PaymentFormListFieldItem item : items) {
            if (TextUtils.equals(value, item.value)) return item;
        }
        return null;
    }

    /**
     * @return значение первого выбранного элемента в списковом поле
     */
    @Nullable
    public String getSelectedValue() {
        PaymentFormListFieldItem selectedItem = getSelectedItem();
        if (selectedItem == null) return null;
        return selectedItem.value;
    }

    /**
     * В зависимости от настройки, для проведения платежа может быть необходим одноразовый код,
     * который в этом случае запрашивается на последней форме.
     *
     * @return Данное поле - это запрос на получение одноразового кода.
     */
    public boolean isOneTimePasswordCodeRequest() {
        return FIELD_TYPE_SCALAR.equals(fieldType) && "$OtpCode".equals(fieldId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fieldType);
        dest.writeString(this.fieldId);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeInt(this.tabOrder);
        dest.writeByte(isRequired ? (byte) 1 : (byte) 0);
        dest.writeString(this.defaultValue);
        dest.writeString(this.example);
        dest.writeString(this.regEx);
        dest.writeInt(this.minLength);
        dest.writeInt(this.maxLength);
        dest.writeTypedList(items);
        dest.writeString(this.value);
    }

    public PaymentFormField() {
    }

    protected PaymentFormField(Parcel in) {
        this.fieldType = in.readString();
        this.fieldId = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.tabOrder = in.readInt();
        this.isRequired = in.readByte() != 0;
        this.defaultValue = in.readString();
        this.example = in.readString();
        this.regEx = in.readString();
        this.minLength = in.readInt();
        this.maxLength = in.readInt();
        this.items = in.createTypedArrayList(PaymentFormListFieldItem.CREATOR);
        this.value = in.readString();
    }

    public static final Creator<PaymentFormField> CREATOR = new Creator<PaymentFormField>() {
        public PaymentFormField createFromParcel(Parcel source) {
            return new PaymentFormField(source);
        }

        public PaymentFormField[] newArray(int size) {
            return new PaymentFormField[size];
        }
    };
}
