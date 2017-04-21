package ru.mail.aslanisl.reminder.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.aslanisl.reminder.R;
import ru.mail.aslanisl.reminder.utils.TaskExample;


public class TasksArrayAdapter extends RecyclerView.Adapter<TasksArrayAdapter.ViewHolder> {

    private static final int SECTION_TYPE = 0;
    private static final int TASK_TYPE = 1;
    private static final String SECTION_SOON = "Скоро";
    private static final String SECTION_TOMORROW = "Завтра";
    private static final String SECTION_TODAY = "Сегодня";
    private static final String SECTION_DONE= "Завершенные";

    private ArrayList<TaskExample> mTasks;
    private SparseArray<Section> mSections;

    public TasksArrayAdapter() {
        mTasks = new ArrayList<>();
        mSections = new SparseArray<>();
    }

    @Override
    public TasksArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean taskType = false;

        if (viewType == TASK_TYPE){
            View taskView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            taskType = true;
            return new ViewHolder(taskView, taskType);
        } else {
            View sectionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sections, parent, false);
            return new ViewHolder(sectionView, taskType);
        }
    }

    @Override
    public void onBindViewHolder(TasksArrayAdapter.ViewHolder holder, int position) {

        if (isSectionHeaderPosition(position)){
            holder.mSectionTextView.setText(String.valueOf(mSections.get(position).getTitle()));
        } else {
            int taskPosition =  sectionedPositionToPosition(position);

            holder.mDataTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getDay()) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getMonth() + 1) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getYear()));

            holder.mDescriptionTextView.setText(mTasks.get(taskPosition).getDescription());

            holder.mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getHour()) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getMinute()));
        }
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

        sortingTasksToSections(mTasks);

        notifyDataSetChanged();
    }

    public void addNewTask (TaskExample taskExample){
        int newTaskPosition = 0;
        for (TaskExample task : mTasks){
            if (task.getTaskDateMillis() > taskExample.getTaskDateMillis()) newTaskPosition++;
        }

        mTasks.add(newTaskPosition, taskExample);

        sortingTasksToSections(mTasks);

        notifyDataSetChanged();
    }

    public void removeTask (int position){
        if (position < getItemCount()) {
            mTasks.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void sortingTasksToSections (ArrayList<TaskExample> tasks){

        int soonTasks = 0;
        boolean isHasSoonTask = false;
        int todayTasks = 0;
        boolean isHasTodayTask = false;
        int doneTasks = 0;
        boolean isHasDoneTask = false;

        Calendar calendar = Calendar.getInstance();

        for (TaskExample task : tasks){
            if (task.getTaskDateMillis() < calendar.getTimeInMillis()){
                doneTasks++;
                isHasDoneTask = true;
            } else if (task.getDay() == calendar.get(Calendar.DAY_OF_MONTH)
                    && task.getMonth() == calendar.get(Calendar.MONTH)
                    && task.getYear() == calendar.get(Calendar.YEAR)){

                todayTasks++;
                isHasTodayTask = true;
            } else {
                soonTasks++;
                isHasSoonTask = true;
            }
        }

        List<Section> sections = new ArrayList<Section>();
        sections.clear();

        if (isHasSoonTask)
            sections.add(new Section(0, SECTION_SOON));

        if (isHasTodayTask)
            sections.add(new Section(soonTasks, SECTION_TODAY));

        if (isHasDoneTask)
            sections.add(new Section(todayTasks + soonTasks, SECTION_DONE));

        mSections.clear();

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.mSectionedPosition = section.mFirstPosition + offset;
            mSections.append(section.mSectionedPosition, section);
            ++offset;
        }
    }

    @Override
    public int getItemCount() {
        if (mTasks == null)
            return 0;
        return mTasks.size() + mSections.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position) ? SECTION_TYPE : TASK_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : getItemId(sectionedPositionToPosition(position));
    }

    public ArrayList<TaskExample> getTasks (){
        return mTasks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mDataTextView;
        TextView mTimeTextView;
        TextView mDescriptionTextView;
        TextView mSectionTextView;

        public ViewHolder(View itemView, boolean taskType) {
            super(itemView);

            if (taskType){
                mDataTextView = (TextView) itemView.findViewById(R.id.data_textView);
                mTimeTextView = (TextView) itemView.findViewById(R.id.time_textView);
                mDescriptionTextView = (TextView) itemView.findViewById(R.id.description_textView);
            } else mSectionTextView = (TextView) itemView.findViewById(R.id.section_text);
        }
    }

    public static class Section {
        int mFirstPosition;
        int mSectionedPosition;
        String mTitle;

        public Section(int firstPosition, String title) {
            this.mFirstPosition = firstPosition;
            this.mTitle = title;
        }

        public String getTitle() {
            return mTitle;
        }
    }

    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.mFirstPosition == o1.mFirstPosition)
                        ? 0
                        : ((o.mFirstPosition < o1.mFirstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.mSectionedPosition = section.mFirstPosition + offset;
            mSections.append(section.mSectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).mFirstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).mSectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

}
