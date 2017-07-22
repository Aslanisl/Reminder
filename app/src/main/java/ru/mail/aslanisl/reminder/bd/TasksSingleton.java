package ru.mail.aslanisl.reminder.bd;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import ru.mail.aslanisl.reminder.App;
import ru.mail.aslanisl.reminder.model.TaskExample;
import ru.mail.aslanisl.reminder.ui.activity.MainActivity;
import ru.mail.aslanisl.reminder.utils.TaskIntentJSONSerializer;

public class TasksSingleton {

    public static final int SOON_TASK = 0;
    public static final int TODAY_TASK = 1;
    public static final int DONE_TASK = 2;

    private static final String TASK_LIST_FILENAME = "taskslist.json";
    private static volatile TasksSingleton mInstance;
    private ArrayList<TaskExample> mTasks;
    private TaskIntentJSONSerializer mSerializer;

    private int mSoonTasks;
    private int mTodayTasks;
    private int mDoneTasks;

    private TasksSingleton() {
        mSerializer = new TaskIntentJSONSerializer(App.get().getApplicationContext(), TASK_LIST_FILENAME);
        mTasks = new ArrayList<>();
        loadTasks();
    }

    public static TasksSingleton getInstance() {
        TasksSingleton localInstance = mInstance;
        if (localInstance == null) {
            synchronized (TasksSingleton.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new TasksSingleton();
                }
            }
        }
        return localInstance;
    }

    public void addTask(TaskExample task){
        mTasks.add(task);
    }

    public void editTest(int position, TaskExample task){
        mTasks.set(position, task);
    }

    public ArrayList<TaskExample> getTasks(){
        return mTasks;
    }

    private void loadTasks(){
        try {
            mTasks.clear();
            mTasks.addAll(mSerializer.loadTasks());
            sortingTasks();
        } catch (Exception e) {
            Log.d(MainActivity.TAG, "Failed to load", e);
        }
    }

    private void sortingTasks(){
        Collections.sort(mTasks, new Comparator<TaskExample>() {
            @Override
            public int compare(TaskExample o1, TaskExample o2) {
                return String.valueOf(o2.getTaskDateMillis()).compareTo(String.valueOf(o1.getTaskDateMillis()));
            }
        });
        mSoonTasks = 0;
        mTodayTasks = 0;
        mDoneTasks = 0;
        Calendar calendar = Calendar.getInstance();
        for (TaskExample task : mTasks) {
            if (task.getTaskDateMillis() < calendar.getTimeInMillis()) {
                mDoneTasks++;
            } else if (task.getDay() == calendar.get(Calendar.DAY_OF_MONTH)
                    && task.getMonth() == calendar.get(Calendar.MONTH)
                    && task.getYear() == calendar.get(Calendar.YEAR)) {
                mTodayTasks++;
            } else {
                mSoonTasks++;
            }
        }
    }

    public boolean saveTasks() {
        try {
            mSerializer.saveTasks(mTasks);
            Log.d(MainActivity.TAG, "tasks saved to file");
            return true;
        } catch (Exception e) {
            Log.d(MainActivity.TAG, "Error saving tasks", e);
            return false;
        }
    }

    public TaskExample getTask(int position){
        if (position >= 0 && position < mTasks.size()){
            return mTasks.get(position);
        } else {
            return null;
        }
    }

    public boolean removeTask(int position){
        if (position >= 0 && position < mTasks.size()){
            mTasks.remove(position);
            return true;
        } else {
            return false;
        }
    }
}
