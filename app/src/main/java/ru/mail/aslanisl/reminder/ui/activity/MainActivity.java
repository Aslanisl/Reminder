package ru.mail.aslanisl.reminder.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.mail.aslanisl.reminder.R;
import ru.mail.aslanisl.reminder.bd.TasksSingleton;
import ru.mail.aslanisl.reminder.model.TaskExample;
import ru.mail.aslanisl.reminder.utils.TaskIntentJSONSerializer;
import ru.mail.aslanisl.reminder.ui.adapter.TasksArrayAdapter;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity implements TasksArrayAdapter.RecyclerItemLongListener, TasksArrayAdapter.RecyclerItemListener {

    public static final String TAG = "myLogs";
    public static final int TASK_REQUEST_CODE = 1;
    public static final int TASK_EDIT_REQUEST_CODE = 2;
    public static final int DEFAULT_VALUE = 1;

    @BindView(R.id.main_activity_toolbar) Toolbar mToolBar;
    @BindView(R.id.main_activity_tasks_recycler) RecyclerView mTasksRecycleView;
    @BindView(R.id.main_activity_no_tasks) TextView mEmptyTask;

    private Drawer mDrawer;

    private TasksSingleton mTasksSingleton;
    private TasksArrayAdapter mTasksAdapter;
    private Paint p = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mToolBar.setTitle(getString(R.string.main_title));
        setSupportActionBar(mToolBar);
        mToolBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
        mToolBar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer();
            }
        });

        initDrawer();

        mTasksRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mTasksAdapter = new TasksArrayAdapter(getApplicationContext(), this, this);
        mTasksRecycleView.setAdapter(mTasksAdapter);

//        initItemTouchHelper(mTasksRecycleView);

        mTasksSingleton = TasksSingleton.getInstance();
        mTasksAdapter.addTasks(mTasksSingleton.getTasks());
        if (mTasksAdapter.getItemCount() == 0) {
            mEmptyTask.setVisibility(View.VISIBLE);
        } else {
            mEmptyTask.setVisibility(View.GONE);
        }
    }

    private void initDrawer(){
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .build();
    }

    @OnClick(R.id.main_activity_new_task)
    void createNewTask() {
        Intent intent;
        intent = new Intent(MainActivity.this, TaskActivity.class);
        startActivityForResult(intent, TASK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            boolean isEditTask = data.getBooleanExtra("edit", false);
            mEmptyTask.setVisibility(View.GONE);
            long timeMillis = data.getLongExtra("date", DEFAULT_VALUE);
            String mTitle = data.getStringExtra("title");
            String mDescription = data.getStringExtra("description");
            TaskExample taskExample = new TaskExample(timeMillis, mTitle, mDescription);
            if (isEditTask) {
                int position = data.getIntExtra("position", 0);
                mTasksAdapter.editTask(position, taskExample);
                mTasksSingleton.editTest(position, taskExample);
            } else {
                mTasksAdapter.addNewTask(taskExample);
                mTasksSingleton.addTask(taskExample);
            }
        } else if (requestCode == TASK_EDIT_REQUEST_CODE && resultCode == RESULT_OK){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void editTask(int position) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task", mTasksAdapter.getTasks().get(mTasksAdapter.sectionedPositionToPosition(position)));
        intent.putExtra("position", position);
        startActivityForResult(intent, TASK_EDIT_REQUEST_CODE);
    }

    public void deleteTask(int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
        mTasksAdapter.removeTask(position);
        mTasksSingleton.removeTask(position);
    }

    //Добавить обрабоку свайпов для ресайклера
    private void initItemTouchHelper(final RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();

                switch (swipeDir) {
                    case ItemTouchHelper.RIGHT:
                        editTask(position);
                        break;
                    case ItemTouchHelper.LEFT:
                        mTasksAdapter.removeTask(position);
                        break;
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_edit_black_24dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_black_24dp);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, null);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onTaskLongClick(int position) {
        createAlertDialog(position);
    }

    private void createAlertDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.alert_dialog_task);
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView editText = (TextView)dialog.findViewById(R.id.alert_dialog_edit_task);
        if (editText != null) {
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTask(position);
                    dialog.dismiss();
                }
            });
        }
        TextView deleteText = (TextView)dialog.findViewById(R.id.alert_dialog_delete_task);
        if (deleteText != null) {
            deleteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteTask(position);
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onTaskClick(int position) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task", mTasksAdapter.getTasks().get(position));
        intent.putExtra("position", position);
        startActivityForResult(intent, TASK_EDIT_REQUEST_CODE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mTasksSingleton.saveTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTasksSingleton.saveTasks();
    }
}
