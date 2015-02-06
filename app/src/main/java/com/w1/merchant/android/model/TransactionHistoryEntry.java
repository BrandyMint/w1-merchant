package com.w1.merchant.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.w1.merchant.android.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by alexey on 07.02.15.
 */
public class TransactionHistoryEntry implements Parcelable {

    public static final String OPERATION_STATE_ACCEPTED = "Accepted";
    public static final String OPERATION_STATE_REJECTED = "Rejected";
    public static final String OPERATION_STATE_CANCELED = "Canceled";
    public static final String OPERATION_STATE_PROCESSING = "Processing";

    public static final String OPERATION_TYPE_PROVIDER_PAYNENT = "ProviderPayment";
    public static final String DIRECTION_INCOMING = "Inc";
    public static final String DIRECTION_OUTCOMING = "Out";

    public static Comparator<TransactionHistoryEntry> SORT_BY_DATE_DESC_DESC_ID_COMPARATOR = new Comparator<TransactionHistoryEntry>() {
        @Override
        public int compare(TransactionHistoryEntry lhs, TransactionHistoryEntry rhs) {
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
                return compareDates != 0 ? compareDates : Utils.compare(lhs.entryId, rhs.entryId);
            }
        }
    };

    /**
     * идентификатор проводки (число неизвестной разрядности)
     *
     */
    public BigInteger entryId = BigInteger.ZERO;

    /**
     * от кошелька
     */
    public BigInteger fromUserId = BigInteger.ZERO;

    /**
     * название отправителя
     */
    public String fromUserTitle = "";

    /**
     * кошельку
     */
    public BigInteger toUserId = BigInteger.ZERO;

    /**
     * название получателя
     */
    public String toUserTitle = "";

    /**
     * сумма
     */
    public BigDecimal amount = BigDecimal.ZERO;

    /**
     * комиссия
     */
    public BigDecimal commissionAmount = BigDecimal.ZERO;

    /**
     * валюта
     */
    public String currencyId = "643";

    /**
     * описание
     */
    public String description = "";

    /**
     * дата создания
     */
    public Date createDate;

    /**
     * дата последнего изменения состояния
     */
    public Date updateDate;

    /**
     * состояние операции
     */
    public String entryStateId = OPERATION_STATE_PROCESSING;

    /**
     * тип операции
     */
    public String operationTypeId = OPERATION_TYPE_PROVIDER_PAYNENT;

    /**
     * идентификатор операции
     */
    public BigInteger operationId = BigInteger.ZERO;

    public boolean isTypeProviderPayment() {
        return OPERATION_TYPE_PROVIDER_PAYNENT.equals(operationTypeId);
    }

    public boolean isAccepted() {
        return OPERATION_STATE_ACCEPTED.equals(entryStateId);
    }

    public boolean isCanceled() {
        return OPERATION_STATE_CANCELED.equals(entryStateId);
    }

    public boolean isRejected() {
        return OPERATION_STATE_REJECTED.equals(entryStateId);
    }

    public boolean isProcessed() { return OPERATION_STATE_PROCESSING.equals(entryStateId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.entryId);
        dest.writeSerializable(this.fromUserId);
        dest.writeString(this.fromUserTitle);
        dest.writeSerializable(this.toUserId);
        dest.writeString(this.toUserTitle);
        dest.writeSerializable(this.amount);
        dest.writeSerializable(this.commissionAmount);
        dest.writeString(this.currencyId);
        dest.writeString(this.description);
        dest.writeLong(createDate != null ? createDate.getTime() : -1);
        dest.writeLong(updateDate != null ? updateDate.getTime() : -1);
        dest.writeString(this.entryStateId);
        dest.writeString(this.operationTypeId);
        dest.writeSerializable(this.operationId);
    }

    public TransactionHistoryEntry() {
    }

    private TransactionHistoryEntry(Parcel in) {
        this.entryId = (BigInteger) in.readSerializable();
        this.fromUserId = (BigInteger) in.readSerializable();
        this.fromUserTitle = in.readString();
        this.toUserId = (BigInteger) in.readSerializable();
        this.toUserTitle = in.readString();
        this.amount = (BigDecimal) in.readSerializable();
        this.commissionAmount = (BigDecimal) in.readSerializable();
        this.currencyId = in.readString();
        this.description = in.readString();
        long tmpCreateDate = in.readLong();
        this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
        long tmpUpdateDate = in.readLong();
        this.updateDate = tmpUpdateDate == -1 ? null : new Date(tmpUpdateDate);
        this.entryStateId = in.readString();
        this.operationTypeId = in.readString();
        this.operationId = (BigInteger) in.readSerializable();
    }

    public static final Parcelable.Creator<TransactionHistoryEntry> CREATOR = new Parcelable.Creator<TransactionHistoryEntry>() {
        public TransactionHistoryEntry createFromParcel(Parcel source) {
            return new TransactionHistoryEntry(source);
        }

        public TransactionHistoryEntry[] newArray(int size) {
            return new TransactionHistoryEntry[size];
        }
    };
}

