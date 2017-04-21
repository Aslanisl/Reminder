package ru.mail.aslanisl.reminder.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.aslanisl.reminder.R;
import ru.mail.aslanisl.reminder.utils.TaskIntentJSONSerializer;
import ru.mail.aslanisl.reminder.ui.adapter.TasksArrayAdapter;
import ru.mail.aslanisl.reminder.utils.TaskExample;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "myLogs";
    public static final String TITLE_NAME = "Reminder";
    public static final int TASK_REQUEST_CODE = 1;
    public static final int DEFAULT_VALUE = 1;
    public static final String TASK_LIST_FILENAME = "taskslist.json";

    @BindView(R.id.main_title_toolbar) TextView mTitleToolBar;
    @BindView(R.id.main_Toolbar) Toolbar mToolBar;
    @BindView(R.id.tasks_recycleView) RecyclerView mTasksRecycleView;

    private TaskIntentJSONSerializer mSerializer;

    private TasksArrayAdapter mTasksAdapter;

    private Paint p = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mSerializer = new TaskIntentJSONSerializer(getApplicationContext(), TASK_LIST_FILENAME);

        //Creating toolbar with TextView
        setSupportActionBar(mToolBar);
        mTitleToolBar.setText(TITLE_NAME);

        mTasksRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mTasksRecycleView.setHasFixedSize(true);
        mTasksRecycleView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mTasksAdapter = new TasksArrayAdapter();

        //Apply this adapter to the RecyclerView
        mTasksRecycleView.setAdapter(mTasksAdapter);

        //инициализация хелпера для ресайкла. Работа со свайпами айтемов.
        initItemTouchHelper(mTasksRecycleView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(MainActivity.this, TaskActivity.class);
                startActivityForResult(intent, TASK_REQUEST_CODE);
            }
        });

        loadTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TASK_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                mTasksAdapter.addNewTask(new TaskExample(data.getLongExtra("date", DEFAULT_VALUE),
                        data.getStringExtra("description")));
            }
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

    @Override
    protected void onPause() {
        super.onPause();
        saveTasks(mTasksAdapter.getTasks());
    }

    public void loadTasks (){
        try {
            mTasksAdapter.addTasks(mSerializer.loadTasks());
        } catch (Exception e){
            mTasksAdapter.addTasks(new ArrayList<TaskExample>());
            Log.d(TAG, "Failed to load", e);
        }
    }

    public boolean saveTasks (ArrayList<TaskExample> tasks) {
        try {
            mSerializer.saveTasks(tasks);
            Log.d(TAG, "tasks saved to file");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error saving tasks", e);
            return false;
        }
    }

    private void initItemTouchHelper (final RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();

                if (viewHolder instanceof TasksArrayAdapter.ViewHolder) {
                    switch (swipeDir){
                        case ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT:
                            mTasksAdapter.removeTask(position);
                            break;
                    }
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft() + 8, (float) itemView.getTop() - 8, dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() - 8 + dX, (float) itemView.getTop() - 8,(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
