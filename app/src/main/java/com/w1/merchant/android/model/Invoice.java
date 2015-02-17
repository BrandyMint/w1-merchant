package com.w1.merchant.android.model;

import android.support.annotation.Nullable;

import com.w1.merchant.android.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

public class Invoice {

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

    public static final String DIRECTION_OUTCOMING = "Out";

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
    public BigInteger toUserId;

    /**
     * Идентификатор контрагента по счету
     */
    public BigInteger userId;

    /**
     * Имя контрагента по счету
     */
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
    //@Nullable
    //public String tags;

    public boolean isInProcessing() {
        return STATE_CREATED.equals(invoiceStateId)
                || STATE_PROCESSING.equals(invoiceStateId)
                || STATE_RECEIVED.equals(invoiceStateId);
    }

    public boolean isPaid() {
        return STATE_ACCEPTED.equals(invoiceStateId);
    }

}
