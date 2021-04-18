package com.android.mqttclient.ui.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.mqttclient.R;
import com.android.mqttclient.model.ReceivedHistory;

import java.util.Arrays;
import java.util.List;

/**
 * RecyclerView class to display list of tasks that received from the middleware broker through MQTT.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<ReceivedHistory> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    /**
     * data is passed into the constructor
     */
    RecyclerViewAdapter(Context context, List<ReceivedHistory> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    /**
     *inflates the row layout from xml when needed.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * binds the data to the TextView in each row
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReceivedHistory task = mData.get(position);
        holder.tvTaskName.setText(formatTaskDetails(task.getTask()));
        holder.tvTaskTime.setText(task.getTime());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Update the list when we receive the executable task.
     */
    public void updateData(List<ReceivedHistory> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * stores and recycles views as they are scrolled off screen
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTaskName;
        TextView tvTaskTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    /**
     * convenience method for getting data at click position
     */
    ReceivedHistory getItem(int id) {
        return mData.get(id);
    }

    /**
     * allows clicks events to be caught
     */
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * parent activity will implement this method to respond to click events
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Format or trim the task ID from the task details and display the rest of the task details on the UI.
     */
    private String formatTaskDetails(String task) {

        StringBuilder stringBuilder = new StringBuilder();

        String[] taskDetails = task.split(";");
        for(int i=1; i<taskDetails.length; i++) {
            stringBuilder.append(taskDetails[i]).append("\n");
        }
        return stringBuilder.toString();
    }
}