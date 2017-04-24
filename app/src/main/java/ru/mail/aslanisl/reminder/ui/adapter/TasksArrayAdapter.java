package ru.mail.aslanisl.reminder.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
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
    private static final String SECTION_TODAY = "Сегодня";
    private static final String SECTION_DONE= "Завершенные";

    private ArrayList<TaskExample> mTasks;
    private SparseArray<Section> mSections;
    private Context mContext;

    public TasksArrayAdapter(Context context) {
        mTasks = new ArrayList<>();
        mSections = new SparseArray<>();
        mContext = context;
    }

    @Override
    public TasksArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TASK_TYPE){
            View taskView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new ViewHolder(taskView);
        } else {
            View sectionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sections, parent, false);
            return new ViewHolder(sectionView);
        }
    }

    @Override
    public void onBindViewHolder(TasksArrayAdapter.ViewHolder holder, int position) {

        if (isSectionHeaderPosition(holder.getAdapterPosition())){
            holder.mSectionTextView.setText(String.valueOf(mSections.get(position).getTitle()));
        } else {
            final int taskPosition =  sectionedPositionToPosition(holder.getAdapterPosition());

            holder.mDataTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getDay()) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getMonth() + 1) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getYear()));

            holder.mDescriptionTextView.setText(mTasks.get(taskPosition).getDescription());

            holder.mTimeTextView.setText(String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getHour()) + ":"
                    + String.format(Locale.ENGLISH, "%02d", mTasks.get(taskPosition).getMinute()));

            holder.mTaskContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showItemDeleteEditDialog(taskPosition);
                    return true;
                }
            });
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
            mTasks.remove(sectionedPositionToPosition(position));
            sortingTasksToSections(mTasks);
            notifyDataSetChanged();
        }
    }

    public void editTask (int position){
        if (position < getItemCount()){

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

        //Sorting tasks by data for soon, today and done.
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

        //Create sections for section header's.
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
        if (mTasks == null) return 0;
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

        @Nullable @BindView(R.id.data_textView) TextView mDataTextView;
        @Nullable @BindView(R.id.time_textView) TextView mTimeTextView;
        @Nullable @BindView(R.id.description_textView) TextView mDescriptionTextView;
        @Nullable @BindView(R.id.task_item_container) RelativeLayout mTaskContainer;
        @Nullable @BindView(R.id.section_text) TextView mSectionTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public void showItemDeleteEditDialog (final int position) {
        new MaterialDialog.Builder(mContext)
                .title(R.string.dialog_task)
                .positiveText("Изменить")
                .negativeText("Удалить")
                .buttonsGravity(GravityEnum.CENTER)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeTask(position);
                    }
                })
                .show();
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
