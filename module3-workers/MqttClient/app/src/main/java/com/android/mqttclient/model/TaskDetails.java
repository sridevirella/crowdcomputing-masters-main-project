package com.android.mqttclient.model;

public class TaskDetails {

    private static String taskId;

    private TaskDetails() {}

    public static void setTaskId(String taskid) {
        taskId = taskid;
    }

    public static String getTaskId(){
        return taskId;
    }
}
