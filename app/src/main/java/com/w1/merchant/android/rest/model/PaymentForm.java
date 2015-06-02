package com.w1.merchant.android.rest.model;

import java.util.Collections;
import java.util.List;

public class PaymentForm {

    /**
     * Идентификатор формы платежа
     */
    public String formId = "";

    /**
     * Название формы платежа
     */
    public String title = "";

    public String description = "";

    public List<Field> fields = Collections.emptyList();


    // XXX RuntimeTypeAdapterFactory?
    public static class Field {

        /**
         * Скалярные. поля, значения которых можно описать одним числом
         * или строкой (номер договора, номер счета, телефона, примечание, ФИО и т.п.);
         */
        public static final String FIELD_TYPE_SCALAR = "Scalar";

        /**
         *  Списковые. поля, значения которых необходимо выбрать из предопределённого
         *  списка. В качестве компонента пользовательского интерфейса рекомендуется
         *  использовать выпадающий список ListBox;
         */
        public static final String FIELD_TYPE_LIST = "List";

        /**
         *  Нередактируемые.  поля, значение которых задано сервисом и не заполняется
         *  пользователем. В качестве компонента пользователького интерфейса
         *  рекомендуется использовать Label.
         */
        public static final String FIELD_TYPE_LABEL = "Label";

        /**
         * Тип поля
         */
        public String fieldType = FIELD_TYPE_SCALAR;


        /**
         * Идентификатор поля
         */
        public String fieldId = "";

        /**
         * Название поля
         */
        public String title = "";

        /**
         * Описание поля
         */
        public String description = "";

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
        public String regex;

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
        public List<ListPaymentFormFieldItem> items;


        /********
         *
         * Value
         *
         */

        public String value;


    }

    public static class ListPaymentFormFieldItem {

        public String title;

        public String value;

        public boolean isSelected;

    }

}
