package com.android.mqttclient.ui.event;

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
import android.widget.Toast;

import com.android.mqttclient.LaunchViewModel;
import com.android.mqttclient.R;
import com.android.mqttclient.model.ReceivedHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that Executed Tasks page display.
 */
public class ExecutedTaskFragment extends Fragment  implements RecyclerViewAdapter.ItemClickListener {

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
        View root = inflater.inflate(R.layout.fragment_executed_tasks, container, false);
        init(root);
        return root;
    }

    /**
     * initViews() method defines all user interface views
     * findViewById is a method that finds the view from the layout resource file of current Activity.
     */
    private void init(View view) {
        recyclerView = view.findViewById(R.id.executed_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        initAdapter();
        adapter.setClickListener(ExecutedTaskFragment.this);
        launchViewModel.getOnNewtaskExecuted().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean s) {
                if(s != null && s) {
                   initAdapter();
                }

            }
        });
    }

    /**
     * Initialize the recycler view adapter.
     */
    private void initAdapter(){
        adapter = new RecyclerViewAdapter(getContext(), launchViewModel.getExecutedTasksFromFile());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * View details of the selected task.
     */
    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getContext(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}