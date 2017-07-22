package ru.mail.aslanisl.reminder.ui.activity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.aslanisl.reminder.model.TaskExample;
import ru.mail.aslanisl.reminder.ui.fragment.DatePickerFragment;
import ru.mail.aslanisl.reminder.utils.NotificationPublisher;
import ru.mail.aslanisl.reminder.ui.fragment.TimePickerFragment;
import ru.mail.aslanisl.reminder.R;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class TaskActivity extends AppCompatActivity implements DatePickerFragment.OnDataPickedListener, TimePickerFragment.OnTimePickedListener {

    private static final int NOTIFICATION_REQUEST_CODE = 1;
    @BindView(R.id.task_activity_tool_bar) Toolbar mToolbar;
    @BindView(R.id.task_activity_data_task_layout) LinearLayout mDataLayout;
    @BindView(R.id.task_activity_data_task_text) TextView mDateTextView;
    @BindView(R.id.task_activity_time_task_layout) LinearLayout mTimeLayout;
    @BindView(R.id.task_activity_time_task_text) TextView mTimeTextView;
    @BindView(R.id.task_activity_title_task_edit_text) EditText mTitleEditText;
    @BindView(R.id.task_activity_description_task_edit_text) EditText mDescriptionEditText;

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

        mNewDateFragment = new DatePickerFragment(this);
        mNewTimeFragment = new TimePickerFragment(this);

        Intent intent = getIntent();
        if (intent.getSerializableExtra("task") == null) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_tasks_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_activity_done:
                createTask();
                break;
        }
        return true;
    }

    @OnClick(R.id.task_activity_data_task_layout)
    void OnDataTask() {
        mNewDateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @OnClick(R.id.task_activity_time_task_layout)
    void OnTimeTask() {
        mNewTimeFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void setCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mDateTextView.setText(String.format(Locale.ENGLISH, "%02d", mDayOfMonth) + ":" + String.format(Locale.ENGLISH, "%02d", mMonth + 1) + ":" + String.valueOf(mYear));
        mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mHourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", mMinute));
    }

    private void setTaskForEdit(Intent intent) {
        TaskExample taskExample = (TaskExample) intent.getSerializableExtra("task");
        mYear = taskExample.getYear();
        mMonth = taskExample.getMonth();
        mDayOfMonth = taskExample.getDay();
        mHourOfDay = taskExample.getHour();
        mMinute = taskExample.getMinute();

        mTitleEditText.setText(taskExample.getTitle());
        mDescriptionEditText.setText(taskExample.getDescription());
        mDateTextView.setText(String.format(Locale.ENGLISH, "%02d", mDayOfMonth) + ":" + String.format(Locale.ENGLISH, "%02d", mMonth + 1) + ":" + String.valueOf(mYear));
        mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mHourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", mMinute));
        mPosition = intent.getIntExtra("position", 0);
        mIsEditTask = true;
    }


    @Override
    public void OnDataPicked(int year, int month, int dayOfMonth) {
        mYear = year;
        mMonth = month;
        mDayOfMonth = dayOfMonth;
        mDateTextView.setText(String.format(Locale.ENGLISH, "%02d", mDayOfMonth) + ":" + String.format(Locale.ENGLISH, "%02d", mMonth + 1) + ":" + String.valueOf(mYear));
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        mHourOfDay = hourOfDay;
        mMinute = minute;
        mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mHourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", mMinute));
    }

    private void createTask() {
        Intent returnIntent = new Intent();
        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDayOfMonth, mHourOfDay, mMinute);
        mTaskTimeMillis = c.getTimeInMillis();
        scheduleNotification(getNotification(mTitleEditText.getText().toString(), mDescriptionEditText.getText().toString()), mTaskTimeMillis);
        returnIntent.putExtra("edit", mIsEditTask);
        returnIntent.putExtra("date", mTaskTimeMillis);
        returnIntent.putExtra("title", mTitleEditText.getText().toString());
        returnIntent.putExtra("description", mDescriptionEditText.getText().toString());
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void scheduleNotification(Notification notification, long taskTime) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, taskTime);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, taskTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(RTC_WAKEUP, taskTime, pendingIntent);
        } else {
            alarmManager.set(RTC_WAKEUP, taskTime, pendingIntent);
        }
    }

    private Notification getNotification(String title, String content) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(NOTIFICATION_REQUEST_CODE, FLAG_UPDATE_CURRENT);
        Resources res = getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.comment_alert);
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        largeIcon = Bitmap.createScaledBitmap(largeIcon, width, height, false);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.comment_alert)
                .setLargeIcon(largeIcon)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return builder.build();
    }
}
