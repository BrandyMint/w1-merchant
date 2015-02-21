package com.w1.merchant.android.model;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.w1.merchant.android.R;
import com.w1.merchant.android.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

public class Invoice implements Parcelable {

    public static Comparator<Invoice> SORT_BY_DATE_DESC_DESC_ID_COMPARATOR = new Comparator<Invoice>() {
        @Override
        public int compare(Invoice lhs, Invoice rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            } else if (lhs == null) {
                return -1;
            } else if (rhs == null) {
                return 1;
            } else {
                long lhsDate = lhs.createDate != null ? lhs.createDate.getTime() : 0;
                long rhsDate = rhs.createDate != null ? rhs.createDate.getTime() : 0;
                int compareDates = Utils.compare(rhsDate, lhsDate);
                return compareDates != 0 ? compareDates : Utils.compare(lhs.invoiceId, rhs.invoiceId);
            }
        }
    };


    public static final String DIRECTION_INCOMING = "Inc";

    public static final String DIRECTION_OUTGOING = "Out";

    /**
     * оплачен
     */
    public static final String STATE_ACCEPTED = "Accepted";

    /**
     * отменен отправителем;
     */
    public static final String STATE_CANCELED = "Canceled";

    /**
     * счет создан, но не получен;
     */
    public static final String STATE_CREATED = "Created";

    /**
     * истек срок действия счета;
     */
    public static final String STATE_EXPIRED = "Expired";

    /**
     *  счет находится в процессе подтверждения;
     */
    public static final String STATE_PROCESSING = "Processing";

    /**
     * счет получен, ожидает принятия;.
     */
    public static final String STATE_RECEIVED = "Received";

    /**
     * счет отвергнут получателем.
     */
    public static final String STATE_REJECTED = "Rejected";


    /**
     * Идентификатор счета, назначенный сервисом
     */
    public BigInteger invoiceId;

    /**
     * Идентификатор отправителя счета
     */
    public BigInteger fromUserId;

    /**
     * Идентификатор получателя счета
     */
    @Nullable
    public BigInteger toUserId;

    /**
     * Идентификатор контрагента по счету
     */
    public BigInteger userId;

    /**
     * Имя контрагента по счету
     */
    @Nullable
    public String userTitle;

    /**
     * Направление счета: Inc - входящий счет, Out - исходящий счет
     */
    public String direction;

    /**
     * Сумма счета
     */
    public BigDecimal amount;

    /**
     * Идентификатор валюты, согласно ISO 4217
     */
    public String currencyId;

    /**
     * Внешний идентификатор счета, передается только отправителю
     */
    public String orderId;

    /**
     * Описание счета
     */
    public String description;

    /**
     * Дата создания счета
     */
    public Date createDate;

    /**
     * Дата последнего изменения счета
     */
    public Date updateDate;

    /**
     * Дата истечения счета
     */
    @Nullable
    public Date expireDate;

    /**
     * Состояние счета:
     * <ul>
     * <li>{@link #STATE_ACCEPTED}</li>
     * <li>{@link #STATE_CANCELED}</li>
     * <li>{@link #STATE_CREATED}</li>
     * <li>{@link #STATE_EXPIRED}</li>
     * <li>{@link #STATE_PROCESSING}</li>
     * <li>{@link #STATE_RECEIVED}</li>
     * <li>{@link #STATE_REJECTED}</li>
     * </ul>
     */
    public String invoiceStateId;

    /**
     * Уплаченная сумма по счету
     */
    public BigDecimal paidAmount;

    /**
     * Пользовательский комментарий к счету (доступен только написавшему его пользователю)
     */
    @Nullable
    public String comment;

    /**
     *  Адрес для перехода на сайт магазина в случае успешной оплаты
     */
    public String successUrl;

    public String legalTitle;

    public String legalTaxNumber;

    public boolean hasSuspense;

    /**
     * Теги счета
     */
    @Nullable
    public String tags;

    public boolean isInProcessing() {
        return STATE_CREATED.equals(invoiceStateId)
                || STATE_PROCESSING.equals(invoiceStateId)
                || STATE_RECEIVED.equals(invoiceStateId);
    }

    public boolean isPaid() {
        return STATE_ACCEPTED.equals(invoiceStateId);
    }

    public boolean isPartiallyPaid() {
        if (hasSuspense) return true;
        if (paidAmount == null || BigDecimal.ZERO.compareTo(paidAmount) == 0) return false;
        return paidAmount.compareTo(amount) < 0;
    }

    public CharSequence getLocalizedDirection(Resources resources) {
        switch (direction) {
            case DIRECTION_INCOMING:
                return resources.getText(R.string.invoice_direction_incoming);
            case DIRECTION_OUTGOING:
                return resources.getText(R.string.invoice_direction_outgoing);
            default:
                return direction;
        }
    }

    public CharSequence getLocalizedInvoiceState(Resources resources) {
        switch (invoiceStateId) {
            case STATE_ACCEPTED:
                return resources.getText(R.string.invoice_state_accepted);
            case STATE_CANCELED:
                return resources.getText(R.string.invoice_state_canceled);
            case STATE_CREATED:
                return resources.getText(R.string.invoice_state_created);
            case STATE_EXPIRED:
                return resources.getText(R.string.invoice_state_expired);
            case STATE_PROCESSING:
                return resources.getText(R.string.invoice_state_processing);
            case STATE_RECEIVED:
                return resources.getText(R.string.invoice_state_received);
            case STATE_REJECTED:
                return resources.getText(R.string.invoice_state_rejected);
            default:
                return invoiceId == null ? null : invoiceStateId;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.invoiceId);
        dest.writeSerializable(this.fromUserId);
        dest.writeSerializable(this.toUserId);
        dest.writeSerializable(this.userId);
        dest.writeString(this.userTitle);
        dest.writeString(this.direction);
        dest.writeSerializable(this.amount);
        dest.writeString(this.currencyId);
        dest.writeString(this.orderId);
        dest.writeString(this.description);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeLong(updateDate != null ? updateDate.getTime() : -1);
        dest.writeLong(expireDate != null ? expireDate.getTime() : -1);
        dest.writeString(this.invoiceStateId);
        dest.writeSerializable(this.paidAmount);
        dest.writeString(this.comment);
        dest.writeString(this.successUrl);
        dest.writeString(this.legalTitle);
        dest.writeString(this.legalTaxNumber);
        dest.writeByte(hasSuspense ? (byte) 1 : (byte) 0);
        dest.writeString(this.tags);
    }

    public Invoice() {
    }

    private Invoice(Parcel in) {
        this.invoiceId = (BigInteger) in.readSerializable();
        this.fromUserId = (BigInteger) in.readSerializable();
        this.toUserId = (BigInteger) in.readSerializable();
        this.userId = (BigInteger) in.readSerializable();
        this.userTitle = in.readString();
        this.direction = in.readString();
        this.amount = (BigDecimal) in.readSerializable();
        this.currencyId = in.readString();
        this.orderId = in.readString();
        this.description = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        long tmpUpdateDate = in.readLong();
        this.updateDate = tmpUpdateDate == -1 ? null : new Date(tmpUpdateDate);
        long tmpExpireDate = in.readLong();
        this.expireDate = tmpExpireDate == -1 ? null : new Date(tmpExpireDate);
        this.invoiceStateId = in.readString();
        this.paidAmount = (BigDecimal) in.readSerializable();
        this.comment = in.readString();
        this.successUrl = in.readString();
        this.legalTitle = in.readString();
        this.legalTaxNumber = in.readString();
        this.hasSuspense = in.readByte() != 0;
        this.tags = in.readString();
    }

    public static final Parcelable.Creator<Invoice> CREATOR = new Parcelable.Creator<Invoice>() {
        public Invoice createFromParcel(Parcel source) {
            return new Invoice(source);
        }

        public Invoice[] newArray(int size) {
            return new Invoice[size];
        }
    };
}
