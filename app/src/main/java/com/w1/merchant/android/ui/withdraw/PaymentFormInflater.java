package com.w1.merchant.android.ui.withdraw;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.thehayro.internal.Constants;
import com.w1.merchant.android.BuildConfig;
import com.w1.merchant.android.R;
import com.w1.merchant.android.rest.model.PaymentForm;
import com.w1.merchant.android.rest.model.PaymentFormField;
import com.w1.merchant.android.rest.model.PaymentFormListFieldItem;
import com.w1.merchant.android.rest.model.SubmitPaymentFormRequest;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey on 14.06.15.
 */
public class PaymentFormInflater {
    private static final boolean DBG = BuildConfig.DEBUG;
    private static final String TAG = Constants.LOG_TAG;

    private static final String FRAGMENT_TAG_DIALOG_SELECT_LIST_FIELD_ITEM = "com.w1.merchant.android.ui.withdraw.PaymentFormInflater.FRAGMENT_TAG_DIALOG_SELECT_LIST_FIELD_ITEM";
    public static final int HEADER_VIEW_COUNT = 1;

    private final ViewGroup mRoot;

    private final LayoutInflater mInflater;

    private final InteractionListener mListener;

    private PaymentForm mTemplate;

    /**
     * Отсортированный по {@linkplain PaymentFormField#tabOrder} список полей, только поддерживаемые поля.
     * Не используем список из mTemplate, так как там сортировка может отличаться от нужной и поля
     * могут быть левые
     */
    private List<PaymentFormField> mSortedFieldList;

    public interface FormSelectActionListener {
        void onFormListFieldItemSelected(PaymentFormField field, String selectedValue);
    }

    public PaymentFormInflater(ViewGroup root, InteractionListener listener) {
        mListener = listener;
        mRoot = root;
        mInflater = LayoutInflater.from(mRoot.getContext());
    }

    public void inflate(PaymentForm form) {
        reset();
        mTemplate = form;
        mSortedFieldList = createSortedFieldList(form);

        mRoot.addView(inflateFormHeader(form));
        PaymentFormField lastField;
        if (mSortedFieldList.isEmpty()) {
            lastField = null;
        } else {
            lastField = mSortedFieldList.get(mSortedFieldList.size() - 1);
        }

        for (PaymentFormField field: mSortedFieldList) {
            FieldInflater fieldInflater;
            boolean isLastField = field == lastField;

            switch (field.fieldType) {
                case PaymentFormField.FIELD_TYPE_SCALAR:
                    fieldInflater = ScalarFieldInflater.inflate(field, mRoot, mListener, isLastField);
                    break;
                case PaymentFormField.FIELD_TYPE_LIST:
                    fieldInflater = ListFieldInflater.inflate(field, mRoot, mListener);
                    break;
                case PaymentFormField.FIELD_TYPE_LABEL:
                    fieldInflater = LabelFieldInflater.inflate(field, mRoot);
                    break;
                default:
                    throw new IllegalStateException();
            }
            if (fieldInflater != null) mRoot.addView(fieldInflater.getRootView());
        }
    }

    public boolean validateForm() {
        if (mTemplate == null) throw new IllegalStateException();
        boolean isFormValid = true;

        for (PaymentFormField field: mSortedFieldList) {
            if (field.fieldType == null) continue;
            FieldInflater inflater = getFieldFormInflater(field);
            isFormValid &= inflater.validate(field, isFormValid);
        }

        return isFormValid;
    }

    public Parcelable getInstanceState() {
        return readForm();
    }

    public void restoreState(Parcelable savedState) {
        setValue((SubmitPaymentFormRequest) savedState);
    }

    private static List<PaymentFormField> createSortedFieldList(PaymentForm form) {
        List<PaymentFormField> sortedFieldList = form.getSortedFieldList();

        List<PaymentFormField> result = new ArrayList<>(sortedFieldList.size());
        // Удаление неподдерживаемых полей
        for (PaymentFormField field: sortedFieldList) {
            if (field.fieldType == null) continue;
            switch (field.fieldType) {
                case PaymentFormField.FIELD_TYPE_SCALAR:
                case PaymentFormField.FIELD_TYPE_LIST:
                case PaymentFormField.FIELD_TYPE_LABEL:
                    result.add(field);
                    break;
                default:
                    if (DBG) Log.v(TAG, "Unknown field type " + field.fieldType);
            }
        }
        return result;
    }

    private static CharSequence appendFieldIsRequiredSymbol(PaymentFormField field, CharSequence title, Context context) {
        if (!field.isRequired) return title;
        SpannableStringBuilder ssb = new SpannableStringBuilder(title);
        ssb.append('\u00a0');
        ssb.append('*');
        TextAppearanceSpan span = new TextAppearanceSpan(context, R.style.ErrorTextAppearance);
        ssb.setSpan(span, ssb.length() - 1, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private void reset() {
        mTemplate = null;
        mSortedFieldList = null;
        mRoot.removeAllViews();
    }

    public void setValue(SubmitPaymentFormRequest data) {
        for (PaymentFormField field: mSortedFieldList) {
            String value = data.findParamValue(field.fieldId);
            if (value != null) {
                FieldInflater inflater = getFieldFormInflater(field);
                inflater.setValue(field, value);
            }
        }
    }

    public SubmitPaymentFormRequest readForm() {
        List<SubmitPaymentFormRequest.Param> params = new ArrayList<>(mSortedFieldList.size());

        for (PaymentFormField field: mSortedFieldList) {
            FieldInflater inflater = getFieldFormInflater(field);
            SubmitPaymentFormRequest.Param param = inflater.readValue(field);
            if (param != null) params.add(param);
        }

        return new SubmitPaymentFormRequest(mTemplate.formId, params);
    }

    void onFormListFieldItemSelected(PaymentFormField field, String selectedValue) {
        ListFieldInflater inflater = (ListFieldInflater)getFieldFormInflater(field);
        inflater.setValue(field, selectedValue);
        inflater.validate(field, false);
    }

    private View getFieldRootView(PaymentFormField field) {
        int fieldIndex = mSortedFieldList.indexOf(field);
        if (fieldIndex < 0) throw new IllegalStateException();
        return mRoot.getChildAt(fieldIndex + HEADER_VIEW_COUNT);
    }

    private FieldInflater getFieldFormInflater(PaymentFormField field) {
        View rootView = getFieldRootView(field);
        return (FieldInflater)rootView.getTag(R.id.tag_inflater_holder);
    }

    private View inflateFormHeader(PaymentForm form) {
        View root = mInflater.inflate(R.layout.payment_form_header, mRoot, false);
        TextView titleView = (TextView)root.findViewById(R.id.payment_form_header_title);
        TextView descriptionView = (TextView)root.findViewById(R.id.payment_form_header_description);

        if (TextUtils.isEmpty(form.description) && TextUtils.isEmpty(form.title)) {
            root.setVisibility(View.GONE);
            return root;
        }

        if (TextUtils.isEmpty(form.title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(form.title);
        }

        if (TextUtils.isEmpty(form.description)) {
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setText(form.description);
        }
        return root;
    }

    private interface FieldInflater {
        View getRootView();
        SubmitPaymentFormRequest.Param readValue(PaymentFormField field);
        void setValue(PaymentFormField field, String value);
        boolean validate(PaymentFormField field, boolean requestFocusOnError);
    }

    private static class ScalarFieldInflater implements FieldInflater {

        private final View mRootView;

        private final TextView mTitleView;

        private final EditText mValueView;

        private final TextView mErrorView;

        private final InputMethodManager mInputMethodManager;

        public static ScalarFieldInflater inflate(final PaymentFormField field,
                                          ViewGroup parent,
                                          InteractionListener listener,
                                          boolean isLastField
                                          ) {
            final View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_form_field_scalar, parent, false);
            ScalarFieldInflater fieldInflater = new ScalarFieldInflater(root);
            fieldInflater.setup(field, listener, isLastField);
            root.setTag(R.id.tag_inflater_holder, fieldInflater);
            return fieldInflater;
        }

        private ScalarFieldInflater(View view) {
            mRootView = view;
            mTitleView = (TextView)view.findViewById(R.id.scalar_field_title);
            mValueView = (EditText)view.findViewById(R.id.scalar_field_value);
            mErrorView = (TextView)view.findViewById(R.id.scalar_field_error_indicator);
            mInputMethodManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        @Override
        public View getRootView() {
            return mRootView;
        }

        public void setup(final PaymentFormField field, final InteractionListener listener, boolean isLastField) {
            mRootView.setContentDescription(field.description);
            mTitleView.setText(appendFieldIsRequiredSymbol(field, field.title, mRootView.getContext()));
            mValueView.setHint(field.example);
            mValueView.setText(field.defaultValue);
            if (field.maxLength > 0) mValueView.setMaxEms(field.maxLength + 1);

            List<InputFilter> inputFilters = new ArrayList<>(3);
            if (field.maxLength > 0) inputFilters.add(new InputFilter.LengthFilter(field.maxLength));

            if (field.isAmountField()) {
                mValueView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                ViewGroup.LayoutParams lp = mValueView.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                mValueView.setLayoutParams(lp);
                mValueView.setMinEms(5);
            }

            if (!inputFilters.isEmpty()) {
                mValueView.setFilters(inputFilters.toArray(new InputFilter[inputFilters.size()]));
            }

            mValueView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        validate(field, false);
                    }
                }
            });

            if (isLastField) {
                mValueView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                mValueView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            listener.onSubmitFormClicked();
                        }
                        return false;
                    }
                });
            }
        }

        @Nullable
        public SubmitPaymentFormRequest.Param readValue(PaymentFormField field) {
            String value = mValueView.getText().toString();
            if (TextUtils.isEmpty(value)) return null;
            return new SubmitPaymentFormRequest.Param(field.fieldId, value);
        }

        @Override
        public void setValue(PaymentFormField field, String value) {
            mValueView.setText(value);
        }

        public boolean validate(PaymentFormField field, boolean requestFocusOnError) {
            // TODO валидация InputMask

            String value = mValueView.getText().toString();

            CharSequence errorText = null;
            Resources resources =  mRootView.getResources();

            if (value.isEmpty()) {
                if (field.isRequired) {
                    errorText = resources.getText(R.string.validate_error_required_field);
                }
            } else {
                if (field.minLength > 0 && value.length() < field.minLength) {
                    errorText = resources.getQuantityString(R.plurals.validate_error_value_must_be_longer_than_symbols,
                            field.minLength, field.minLength);
                } else if (field.maxLength > 0 && value.length() > field.maxLength) {
                    errorText = resources.getQuantityString(R.plurals.validate_error_value_must_be_shorter_than_symbols,
                            field.maxLength, field.maxLength);
                } else if (!TextUtils.isEmpty(field.regEx) && !value.matches(field.regEx)) {
                    errorText = resources.getText(R.string.validate_error_value_has_wrong_format);
                }
            }

            setError(field, errorText);
            if (errorText != null && requestFocusOnError) {
                mValueView.requestFocusFromTouch();
                mRootView.requestRectangleOnScreen(new Rect(0,0, mRootView.getWidth(), mRootView.getHeight()), true);
                mInputMethodManager.showSoftInput(mValueView, 0);
            }
            return errorText == null;
        }

        private void setError(PaymentFormField field, @Nullable CharSequence error) {
            if (error == null) {
                mErrorView.setVisibility(View.INVISIBLE);
            } else {
                mErrorView.setVisibility(View.VISIBLE);
                mErrorView.setText(error);
            }
        }
    }

    private static class ListFieldInflater implements FieldInflater {
        private final View mRootView;

        private final TextView mTitleView;

        private final TextView mValueView;

        private final TextView mErrorView;

        private String mValue;

        public static ListFieldInflater inflate(final PaymentFormField field,
                                                  ViewGroup parent,
                                                  InteractionListener listener) {
            final View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_form_field_list, parent, false);
            ListFieldInflater fieldInflater = new ListFieldInflater(root);
            fieldInflater.setup(field, listener);
            root.setTag(R.id.tag_inflater_holder, fieldInflater);
            return fieldInflater;
        }

        private ListFieldInflater(View root) {
            mRootView = root;
            mTitleView = (TextView)root.findViewById(R.id.list_field_title);
            mValueView = (TextView)root.findViewById(R.id.list_field_value);
            mErrorView = (TextView)root.findViewById(R.id.list_field_error_indicator);
        }

        @Override
        public View getRootView() {
            return mRootView;
        }

        public void setup(final PaymentFormField field, final InteractionListener listener) {
            mRootView.setContentDescription(field.description);
            mTitleView.setText(appendFieldIsRequiredSymbol(field, field.title, mTitleView.getContext()));

            PaymentFormListFieldItem selectedItem = field.getSelectedItem();
            if (selectedItem != null) {
                mValueView.setText(selectedItem.title);
            } else {
                mValueView.setText(R.string.touch_to_choose_field_value);
            }

            mValueView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectItemDialog(field, listener);
                }
            });
        }

        @Override
        public void setValue(PaymentFormField field, String value) {
            mValue = value;
            PaymentFormListFieldItem fieldItem = field.findListItem(value);
            if (fieldItem == null) throw new IllegalStateException();
            mValueView.setText(fieldItem.title);
        }

        public boolean validate(PaymentFormField field, boolean requestFocusOnError) {
            SubmitPaymentFormRequest.Param value = readValue(field);
            CharSequence errorText = null;
            Resources resources =  mRootView.getResources();

            if (value == null && field.isRequired) {
                errorText = resources.getText(R.string.validate_error_required_field);
            }

            setError(errorText);
            if (errorText != null && requestFocusOnError) {
                mRootView.requestFocusFromTouch();
                mRootView.requestRectangleOnScreen(new Rect(0, 0, 0, 0));
            }
            return errorText == null;
        }

        private void setError(@Nullable CharSequence error) {
            if (error == null) {
                mErrorView.setVisibility(View.INVISIBLE);
            } else {
                mErrorView.setVisibility(View.VISIBLE);
                mErrorView.setText(error);
            }
        }

        @Nullable
        public SubmitPaymentFormRequest.Param readValue(PaymentFormField field) {
            if (mValue == null) {
                mValue = field.getSelectedValue();
                if (mValue == null) return null;
            }
            return new SubmitPaymentFormRequest.Param(field.fieldId, mValue);
        }

        private void showSelectItemDialog(PaymentFormField field, InteractionListener listener) {
            SelectListFieldItemDialog dialogFragment = SelectListFieldItemDialog.createDialog(field);

            FragmentManager fm = listener.getFragmentManager();
            Fragment old = fm.findFragmentByTag(FRAGMENT_TAG_DIALOG_SELECT_LIST_FIELD_ITEM);
            if (old != null) fm.beginTransaction().remove(old).commitAllowingStateLoss();
            dialogFragment.show(fm, FRAGMENT_TAG_DIALOG_SELECT_LIST_FIELD_ITEM);
        }
    }

    private static class LabelFieldInflater implements FieldInflater {

        private final View mRootView;

        private final TextView mTitleView;

        private final TextView mValueView;


        public static LabelFieldInflater inflate(final PaymentFormField field,
                                                ViewGroup parent) {
            final View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_form_field_label, parent, false);
            LabelFieldInflater fieldInflater = new LabelFieldInflater(root);
            fieldInflater.setup(field);
            root.setTag(R.id.tag_inflater_holder, fieldInflater);
            return fieldInflater;
        }

        private LabelFieldInflater(View root) {
            mRootView = root;
            mTitleView = (TextView)root.findViewById(R.id.label_field_title);
            mValueView = (TextView)root.findViewById(R.id.label_field_value);
        }

        @Override
        public View getRootView() {
            return mRootView;
        }

        public void setup(PaymentFormField field) {
            mTitleView.setText(field.title);
            mValueView.setText(field.value);
        }

        @Override
        public SubmitPaymentFormRequest.Param readValue(PaymentFormField field) {
            return null;
        }

        @Override
        public void setValue(PaymentFormField field, String value) {
        }

        @Override
        public boolean validate(PaymentFormField field, boolean requestFocusOnError) {
            return true;
        }
    }

    public static class SelectListFieldItemDialog extends DialogFragment {

        private static final String ARG_FORM_FIELD = "com.w1.merchant.android.ui.withdraw.PaymentFormInflater.SelectListFieldItemDialog.ARG_FORM_FIELD";

        private FormSelectActionListener mListener;

        public static SelectListFieldItemDialog createDialog(PaymentFormField field) {
            Assert.assertEquals(PaymentFormField.FIELD_TYPE_LIST, field.fieldType);
            SelectListFieldItemDialog dialog = new SelectListFieldItemDialog();
            Bundle bundle = new Bundle(2);
            bundle.putParcelable(ARG_FORM_FIELD, field);
            dialog.setArguments(bundle);
            return dialog;
        }

        public SelectListFieldItemDialog() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (FormSelectActionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement FormSelectActionListener");
            }
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final PaymentFormField field = getArguments().getParcelable(ARG_FORM_FIELD);

            final CharSequence dialogListItems[] = new CharSequence[field.items.size()];
            for (int i=0, listSize = field.items.size(); i < listSize; ++i) {
                dialogListItems[i] = field.items.get(i).title;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setTitle(field.title)
                    .setCancelable(true)
                    .setItems(dialogListItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SelectListFieldItemDialog.this.onItemChecked(field, field.items.get(which).value);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    ;
            return builder.create();
        }

        void onItemChecked(PaymentFormField field, String value) {
            if (mListener != null) mListener.onFormListFieldItemSelected(field, value);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;
        }
    }

    public interface InteractionListener {
        FragmentManager getFragmentManager();
        void onSubmitFormClicked();
    }


}
