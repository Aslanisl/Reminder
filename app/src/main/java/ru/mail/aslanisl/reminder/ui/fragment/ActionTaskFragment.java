package ru.mail.aslanisl.reminder.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.mail.aslanisl.reminder.R;
import ru.mail.aslanisl.reminder.ui.activity.MainActivity;

public class ActionTaskFragment extends DialogFragment {

    private boolean mIsEditTask = false;
    private boolean mIsDeleteTask = false;
    private int mPosition;

    Unbinder mUnbinder;

    public static ActionTaskFragment newInstance (int position){
        ActionTaskFragment fragment = new ActionTaskFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_action_task, container, false);

        mPosition = getArguments().getInt("position");

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.edit_task)
    void editTask(){
        ((MainActivity)getActivity()).editTask(mPosition);
        dismiss();
    }

    @OnClick(R.id.delete_task)
    void deleteTask(){
        ((MainActivity)getActivity()).deleteTask(mPosition);
        dismiss();
    }
}
