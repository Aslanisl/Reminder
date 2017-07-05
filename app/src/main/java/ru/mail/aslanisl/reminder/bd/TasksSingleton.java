package ru.mail.aslanisl.reminder.bd;

import android.util.Log;

import java.util.ArrayList;

import ru.mail.aslanisl.reminder.App;
import ru.mail.aslanisl.reminder.model.TaskExample;
import ru.mail.aslanisl.reminder.ui.activity.MainActivity;
import ru.mail.aslanisl.reminder.utils.TaskIntentJSONSerializer;

public class TasksSingleton {

    private static final String TASK_LIST_FILENAME = "taskslist.json";
    private static volatile TasksSingleton mInstance;
    private ArrayList<TaskExample> mTasks;
    private TaskIntentJSONSerializer mSerializer;

    private TasksSingleton() {
        mSerializer = new TaskIntentJSONSerializer(App.get().getApplicationContext(), TASK_LIST_FILENAME);
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

    public ArrayList<TaskExample> getTasks(){
        try {
            mTasks.addAll(mSerializer.loadTasks());
        } catch (Exception e) {
            Log.d(MainActivity.TAG, "Failed to load", e);
        }
        return mTasks;
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
