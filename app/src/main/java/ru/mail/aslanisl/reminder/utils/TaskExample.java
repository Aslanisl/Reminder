package ru.mail.aslanisl.reminder.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;

public class TaskExample implements Serializable{

    public static final String JSON_TIME = "time";
    public static final String JSON_DESCRIPTION = "description";

    long mTaskDateMillis;
    String mDescription;
    Calendar c;

    public TaskExample (long dateMillis, String description) {
        this.mTaskDateMillis = dateMillis;
        this.mDescription = description;
        c = Calendar.getInstance();
        c.setTimeInMillis(dateMillis);
    }

    public TaskExample (JSONObject json) throws JSONException{
        mTaskDateMillis = json.getLong(JSON_TIME);
        mDescription = json.getString(JSON_DESCRIPTION);
        c = Calendar.getInstance();
        c.setTimeInMillis(mTaskDateMillis);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_TIME, mTaskDateMillis);
        json.put(JSON_DESCRIPTION, mDescription);
        return json;
    }

    public int getYear (){
        return c.get(Calendar.YEAR);
    }

    public int getMonth (){
        return c.get(Calendar.MONTH);
    }

    public int getDay () {
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour () {
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute () {
        return c.get(Calendar.MINUTE);
    }

    public long getTaskDateMillis() {
        return mTaskDateMillis;
    }

    public String getDescription() {
        return mDescription;
    }
}
