package com.android.mqttclient.ui.event;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mqttclient.LaunchViewModel;
import com.android.mqttclient.LauncherActivity;
import com.android.mqttclient.R;
import com.android.mqttclient.model.Constants;
import com.android.mqttclient.model.Message;
import com.android.mqttclient.model.ReceivedHistory;
import com.android.mqttclient.services.ReadWriteFile;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that Received Tasks page display.
 */
public class ReceivedTaskFragment  extends Fragment implements RecyclerViewAdapter.ItemClickListener {

    private LaunchViewModel launchViewModel;
    RecyclerViewAdapter adapter;
    RecyclerView recyclerView;

    /**
     * method is invoked to create fragment view object via XML layout inflation.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        launchViewModel =
                ViewModelProviders.of(requireActivity()).get(LaunchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_received_tasks, container, false);
        init(root);
        return root;
    }

    /**
     * initViews() method defines all user interface views
     * findViewById is a method that finds the view from the layout resource file of current Activity.
     */
    private void init(View view) {
        recyclerView = view.findViewById(R.id.received_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initAdapter();
        adapter.setClickListener(ReceivedTaskFragment.this);
        launchViewModel.getNewTaskReceived().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                adapter.updateData(launchViewModel.getReceivedTasksFromFile());
            }
        });
    }

    /**
     * Initialize the recycler view adapter.
     */
    private void initAdapter(){
        adapter = new RecyclerViewAdapter(getContext(), launchViewModel.getReceivedTasksFromFile());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Handle the user selected task.
     */
    @Override
    public void onItemClick(View view, int position) {
        ReceivedHistory row = adapter.getItem(position);

        if(row.getDuration()>0) {
            showDialog(row.getTask(), "subTask", row.getDuration(), row.getTask().split(";")[0].split(":")[1]);
        } else {
            String downloadFile = row.getDownloadFile();
            if(downloadFile != null && !downloadFile.equals("null")) {
                showDialog(row.getTask(), downloadFile, row.getTask().split(";")[0]);
            } else {
                Toast.makeText(getContext(), "The task you selected has expired on row number " + position, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Display pop dialog to run the executable file.
     */
    private void showDialog(String description, final String downloadFile, final String taskId){

        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.file_dialog_layout);

            TextView text = dialog.findViewById(R.id.task_desc_tv2);
            text.setText(description);

            Button acceptButton = dialog.findViewById(R.id.accept_btn2);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReadWriteFile rwfInstance = new ReadWriteFile(getContext());
                    rwfInstance.initiateInstallation(downloadFile, taskId);
                    dialog.dismiss();
                }
            });
            Button denyButton = dialog.findViewById(R.id.deny_btn2);
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    /**
     * Display pop dialog to accept the task.
     */
    private void showDialog(String taskDesc, String subTask, int duration, final String taskId){

        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_layout);

            TextView heading = dialog.findViewById(R.id.task_heading_tv);
            heading.setText( getString(R.string.received_pending_task_expires_in_about_1_s, duration));
            TextView text = dialog.findViewById(R.id.task_desc_tv);
            text.setText(taskDesc);

            Button acceptButton = dialog.findViewById(R.id.accept_btn1);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchViewModel.getTaskHandlerObj().taskResponse(Constants.WORKER_ID.getConstant(), "Yes", taskId); // set task response as Yes
                    dialog.dismiss();
                }
            });
            Button denyButton = dialog.findViewById(R.id.deny_btn1);
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }
}