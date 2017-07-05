package ru.mail.aslanisl.reminder.ui.activity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.aslanisl.reminder.model.TaskExample;
import ru.mail.aslanisl.reminder.ui.fragment.DatePickerFragment;
import ru.mail.aslanisl.reminder.utils.NotificationPublisher;
import ru.mail.aslanisl.reminder.ui.fragment.TimePickerFragment;
import ru.mail.aslanisl.reminder.R;

import static android.R.attr.id;
import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.task_activity_tool_bar) Toolbar mToolbar;
    @BindView(R.id.task_activity_data_task_layout) LinearLayout mDataLayout;
    @BindView(R.id.task_activity_data_task_text) TextView mDateTextView;
    @BindView(R.id.task_activity_time_task_layout) LinearLayout mTimeLayout;
    @BindView(R.id.task_activity_time_task_text) TextView mTimeTextView;
    @BindView(R.id.task_activity_task_create_button) Button mTaskCreateButton;
    @BindView(R.id.task_activity_description_task_editText) EditText mDescriptionEditText;

    private DatePickerFragment mNewDateFragment;
    private TimePickerFragment mNewTimeFragment;
    private long mTaskTimeMillis;
    private int mYear;
    private int mMonth;
    private int mDayOfMonth;
    private int mHourOfDay;
    private int mMinute;
    private boolean mIsEditTask = false;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        ButterKnife.bind(this);

        mDataLayout.setOnClickListener(this);
        mTimeLayout.setOnClickListener(this);
        mTaskCreateButton.setOnClickListener(this);

        mNewDateFragment = new DatePickerFragment(mDateTextView);
        mNewTimeFragment = new TimePickerFragment(mTimeTextView);

        Intent intent = getIntent();
        if (intent == null) {
            setCurrentDateTime();
            mToolbar.setTitle(getString(R.string.new_task_title));
        } else {
            setTaskForEdit(intent);
            mToolbar.setTitle(getString(R.string.edit_task_title));
        }

        mToolbar.setNavigationIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_white_24dp));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.task_activity_data_task_layout:
                mNewDateFragment.show(getSupportFragmentManager(), "datePicker");
                break;
            case R.id.task_activity_time_task_layout:
                mNewTimeFragment.show(getSupportFragmentManager(), "timePicker");
                break;
            case R.id.task_activity_task_create_button:
                createTask();
                break;
        }
    }

    private void setCurrentDateTime (){
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        mDateTextView.setText(String.format(Locale.ENGLISH, "%02d", mDayOfMonth) + ":" + String.format(Locale.ENGLISH, "%02d", mMonth + 1) + ":" + String.valueOf(mYear));
        mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mHourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", mMinute));
    }

    private void setTaskForEdit(Intent intent){
        if (intent.getSerializableExtra("task") != null) {
            TaskExample taskExample = (TaskExample) intent.getSerializableExtra("task");
            mYear = taskExample.getYear();
            mMonth = taskExample.getMonth();
            mDayOfMonth = taskExample.getDay();
            mHourOfDay = taskExample.getHour();
            mMinute = taskExample.getMinute();

            mDescriptionEditText.setText(taskExample.getDescription());
            mDateTextView.setText(String.format(Locale.ENGLISH, "%02d", mDayOfMonth) + ":" + String.format(Locale.ENGLISH, "%02d", mMonth + 1) + ":" + String.valueOf(mYear));
            mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mHourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", mMinute));

            mPosition = intent.getIntExtra("position", 0);
            mIsEditTask = true;
        }
    }

    private void createTask () {

        //Чтоты в ситуации, когда не выбрано время пользователем, было текущее время
        mYear = mNewDateFragment.getYear() == -1 ? mYear : mNewDateFragment.getYear();
        mMonth = mNewDateFragment.getMonth() == -1 ? mMonth : mNewDateFragment.getMonth();
        mDayOfMonth = mNewDateFragment.getDayOfMonth() == -1 ? mDayOfMonth : mNewDateFragment.getDayOfMonth();
        mHourOfDay = mNewTimeFragment.getHourOfDay() == -1 ? mHourOfDay : mNewTimeFragment.getHourOfDay();
        mMinute = mNewTimeFragment.getMinute() == -1 ? mMinute : mNewTimeFragment.getMinute();

        Intent returnIntent = new Intent();

        Calendar c = Calendar.getInstance();

        c.set(mYear, mMonth, mDayOfMonth, mHourOfDay, mMinute);
        mTaskTimeMillis = c.getTimeInMillis();

        scheduleNotification(getNotification(mDescriptionEditText.getText().toString()), mTaskTimeMillis);

        returnIntent.putExtra("edit", mIsEditTask);
        returnIntent.putExtra("date", mTaskTimeMillis);
        returnIntent.putExtra("description", mDescriptionEditText.getText().toString());
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();

        Calendar c = Calendar.getInstance();

        c.set(mYear, mMonth, mDayOfMonth, mHourOfDay, mMinute);
        mTaskTimeMillis = c.getTimeInMillis();

        scheduleNotification(getNotification(mDescriptionEditText.getText().toString()), mTaskTimeMillis);

        returnIntent.putExtra("edit", mIsEditTask);
        returnIntent.putExtra("date", mTaskTimeMillis);
        returnIntent.putExtra("description", mDescriptionEditText.getText().toString());
        setResult(RESULT_CANCELED, returnIntent);
        finish();

        super.onBackPressed();
    }

    private void scheduleNotification(Notification notification, long taskTime) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, taskTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(RTC_WAKEUP, taskTime, pendingIntent);
        } else {
            alarmManager.set(RTC_WAKEUP, taskTime , pendingIntent);
        }
    }

    private Notification getNotification(String content) {

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(id, FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Необходимо сделать:")
                .setContentText(content)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.comment_alert)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        return builder.build();
    }
}
