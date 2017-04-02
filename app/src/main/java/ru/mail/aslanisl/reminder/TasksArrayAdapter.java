package ru.mail.aslanisl.reminder;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.aslanisl.reminder.taskPacket.TaskExample;


public class TasksArrayAdapter extends RecyclerView.Adapter<TasksArrayAdapter.ViewHolder> {

    private ArrayList<TaskExample> mTasks;

    public TasksArrayAdapter() {
        mTasks = new ArrayList<>();
    }

    @Override
    public TasksArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TasksArrayAdapter.ViewHolder holder, final int position) {

        holder.mDataTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(position).getDay()) + ":"
                + String.format(Locale.ENGLISH, "%02d", mTasks.get(position).getMonth()) + ":"
                + String.format(Locale.ENGLISH, "%02d", mTasks.get(position).getYear()));

        holder.mDescriptionTextView.setText(mTasks.get(position).getDescription());

        holder.mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(position).getHour()) + ":"
                + String.format(Locale.ENGLISH, "%02d", mTasks.get(position).getMinute()));
    }

    public void addTasks(ArrayList<TaskExample> taskList) {
        mTasks.clear();
        mTasks.addAll(taskList);
        Collections.sort(mTasks, new Comparator<TaskExample>() {
            @Override
            public int compare(TaskExample o1, TaskExample o2) {
                return String.valueOf(o2.getTaskDateMillis()).compareTo(String.valueOf(o1.getTaskDateMillis()));
            }
        });
        notifyDataSetChanged();
    }

    public void removeTask (int position){
        mTasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mTasks.size());
    }

    @Override
    public int getItemCount() {
        if (mTasks == null)
            return 0;
        return mTasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.data_textView) TextView mDataTextView;
        @BindView(R.id.time_textView) TextView mTimeTextView;
        @BindView(R.id.description_task_textView) TextView mDescriptionTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
