package ru.mail.aslanisl.reminder;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import ru.mail.aslanisl.reminder.taskPacket.TaskExample;

public class TaskIntentJSONSerializer {
    private Context mContext;
    private String mFilename;

    public TaskIntentJSONSerializer (Context context, String filename){
        this.mContext = context;
        this.mFilename = filename;
    }

    public void saveTasks(ArrayList<TaskExample> tasks) throws JSONException, IOException {
        //Построение массива в JSON
        JSONArray array = new JSONArray();
        for (TaskExample c : tasks)
            array.put(c.toJSON());

        //Запись файла на диск
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public ArrayList<TaskExample> loadTasks () throws IOException, JSONException {
        ArrayList<TaskExample> tasks = new ArrayList<>();
        BufferedReader reader = null;
        try {
            // Открытие и чтение файла в String Buffer
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                jsonString.append(line);
            }

            //Разбор JSON с использованием JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            //Построение массива объектов
            for (int i = 0; i < array.length(); i++){
                tasks.add(new TaskExample(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } finally {
            if (reader != null)
                reader.close();
        }
        return tasks;
    }
}
