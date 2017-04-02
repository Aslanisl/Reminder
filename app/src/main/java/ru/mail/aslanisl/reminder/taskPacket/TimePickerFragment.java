package ru.mail.aslanisl.reminder.taskPacket;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    TextView mTextView;
    private int mHourOfDay = -1;
    private int mMinute = -1;

    public TimePickerFragment(TextView textView){
        mTextView = textView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hourOfDay, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTextView.setText(String.format(Locale.ENGLISH, "%02d", hourOfDay) +
                ":" + String.format(Locale.ENGLISH, "%02d", minute));

        mHourOfDay = hourOfDay;
        mMinute = minute;

        Calendar c = Calendar.getInstance();
        c.set(0, 0, 0, hourOfDay, minute);
    }

    public int getHourOfDay() {
        return mHourOfDay;
    }

    public int getMinute() {
        return mMinute;
    }
}
