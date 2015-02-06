package com.w1.merchant.android.model;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.w1.merchant.android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Schedule {

    public int utcTimeOffset;

    public Once once;

    public Daily daily;

    public Weekly weekly;

    public MonthlyDayOfMonth monthlyDayOfMonth;

    public MonthlyDaysOfWeek monthlyDaysOfWeek;

    @Nullable
    private Date nextExecution;

    @Nullable
    public Integer executedTimes;

    /**
     * Однократное
     */
    public static class Once {
        Date startDate;
    }

    /**
     * По дням
     */
    public static class Daily {
        public Date startDate;

        @Nullable
        public Date endDate;
    }

    public static class Weekly {

        public Date startDate;

        @Nullable
        public Date endDate;

        /**
         * Максимальное количество исполнений
         */
        public int maxExecutions;

        /**
         * Промежуток между неделями
         */
        public int interval;

        public DaysOfWeek daysOfWeek;
    }

    /**
     * В опеределенное число месяца
     */
    public static class MonthlyDayOfMonth {

        public static final int DAY_OF_MONTH_LAST_DAY = 32;

        public Date startDate;

        @Nullable
        public Date endDate;

        /**
         * Максимальное количество исполнений
         */
        public int maxExecutions;

        /**
         * Промежуток между неделями
         */
        public int interval;

        /**
         * Номер дня в месяце. Спец. значение - “32” - последний день месяца.
         */
        public int dayOfMonth;

    }

    /**
     * В опеределенные дни недели месяца
     */
    public static class MonthlyDaysOfWeek {
        public Date startDate;

        @Nullable
        public Date endDate;

        /**
         * Максимальное количество исполнений
         */
        public int maxExecutions;

        /**
         * Промежуток между месяцами
         */
        public int interval;

        public int weekOfMonth;

        public DaysOfWeek daysOfWeek;
    }


    /**
     * Список дней недели
     */
    public static class DaysOfWeek {
        public boolean sunday;

        public boolean monday;

        public boolean tuesday;

        public boolean wednesday;

        public boolean thursday;

        public boolean friday;

        public boolean saturday;
    }


    private static final SimpleDateFormat sDateDMYDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
    private static final SimpleDateFormat sHhMmDateFormat = new SimpleDateFormat("HH:mm", Locale.US);

    @Nullable
    public Date getNextExecutionDate() {
        if (nextExecution == null) return null;
        return new Date(nextExecution.getTime() + utcTimeOffset * 60 * 1000);
    }

    public CharSequence getDescription(Resources resources) {

        //однократно
        if (this.once != null) {
            return resources.getString(R.string.payment_one,
                    sDateDMYDateFormat.format(this.once.startDate),
                    sHhMmDateFormat.format(this.once.startDate)
            );

        }

        //ежедневно
        if (this.daily != null) {
            if (this.daily.endDate == null) {
                return resources.getString(R.string.payment_begin,
                        " " + resources.getString(R.string.daily) + ", ",
                        sHhMmDateFormat.format(this.daily.startDate),
                        sDateDMYDateFormat.format(this.daily.startDate));
            } else {
                return resources.getString(R.string.payment_begin_end,
                        " " + resources.getString(R.string.daily) + ", ",
                        sHhMmDateFormat.format(this.daily.startDate),
                        sDateDMYDateFormat.format(this.daily.startDate),
                        sDateDMYDateFormat.format(this.daily.endDate),
                        sHhMmDateFormat.format(this.daily.endDate));
            }
        }


        //еженедельно
        if (this.weekly != null) {
            List<String> days = new ArrayList<>(7);
            if (this.weekly.daysOfWeek.monday) days.add(resources.getString(R.string.monday));
            if (this.weekly.daysOfWeek.tuesday) days.add(resources.getString(R.string.tuesday));
            if (this.weekly.daysOfWeek.wednesday) days.add(resources.getString(R.string.wednesday));
            if (this.weekly.daysOfWeek.thursday) days.add(resources.getString(R.string.thursday));
            if (this.weekly.daysOfWeek.friday) days.add(resources.getString(R.string.friday));
            if (this.weekly.daysOfWeek.saturday) days.add(resources.getString(R.string.saturday));
            if (this.weekly.daysOfWeek.sunday) days.add(resources.getString(R.string.sunday));
            if (this.weekly.endDate == null) {
                return resources.getString(R.string.payment_begin,
                        " " + resources.getString(R.string.weekly) + ", " +
                                resources.getString(R.string.on_, TextUtils.join(", ", days)),
                        sHhMmDateFormat.format(this.weekly.startDate),
                        sDateDMYDateFormat.format(this.weekly.startDate));
            } else {
                return resources.getString(R.string.payment_begin_end,
                        " " + resources.getString(R.string.weekly) + ", " +
                                resources.getString(R.string.on_, TextUtils.join(", ", days)),
                        sHhMmDateFormat.format(this.daily.startDate),
                        sDateDMYDateFormat.format(this.daily.startDate),
                        sDateDMYDateFormat.format(this.daily.endDate),
                        sHhMmDateFormat.format(this.daily.endDate));
            }
        }

        //ежемесячно
        if (this.monthlyDayOfMonth != null) {
            if (this.monthlyDayOfMonth.dayOfMonth == 1) { //первый день
                if (this.monthlyDayOfMonth.endDate == null) {
                    return resources.getString(R.string.payment_begin,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.first_day),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate));
                } else {
                    return resources.getString(R.string.payment_begin_end,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.first_day),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.endDate),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.endDate)
                    );
                }
            } else if (this.monthlyDayOfMonth.dayOfMonth == MonthlyDayOfMonth.DAY_OF_MONTH_LAST_DAY) { //последний день
                if (this.monthlyDayOfMonth.endDate == null) {
                    return resources.getString(R.string.payment_begin,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.last_day),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate));
                } else {
                    return resources.getString(R.string.payment_begin_end,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.last_day),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.endDate),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.endDate)
                    );
                }
            } else { //остальные
                if (this.monthlyDayOfMonth.endDate == null) {
                    return resources.getString(R.string.payment_begin,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.n_day, this.monthlyDayOfMonth.dayOfMonth),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate));
                } else {
                    return resources.getString(R.string.payment_begin_end,
                            " " + resources.getString(R.string.monthly) + " " +
                                    resources.getString(R.string.n_day, this.monthlyDayOfMonth.dayOfMonth),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.startDate),
                            sDateDMYDateFormat.format(this.monthlyDayOfMonth.endDate),
                            sHhMmDateFormat.format(this.monthlyDayOfMonth.endDate)
                    );
                }
            }
        }
        return "";
    }

}
