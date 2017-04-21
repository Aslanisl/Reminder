package ru.mail.aslanisl.reminder.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    TextView mTextView;
    private long mDateTimeMillis = -1;
    private int mYear = -1;
    private int mMonth = -1;
    private int mDayOfMonth = -1;

    public DatePickerFragment(TextView mTextView) {
        this.mTextView = mTextView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mTextView.setText(String.format(Locale.ENGLISH, "%02d", dayOfMonth) + ":"
                + String.format(Locale.ENGLISH, "%02d", month + 1) + ":" + String.valueOf(year));

        mYear = year;
        mMonth = month;
        mDayOfMonth = dayOfMonth;

        Calendar c = Calendar.getInstance();

        c.set(year, month, dayOfMonth);

        mDateTimeMillis = c.getTimeInMillis();
    }

    public int getYear() {
        return mYear;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }
}
