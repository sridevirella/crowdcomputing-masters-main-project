package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public class SubTask {

    @PrimaryKey
    private TaskWorkerCompositeKey subTaskKey;
    private String subTask;
    private String subTaskResult;
    private boolean processed;

    public SubTask() {}
    public SubTask(TaskWorkerCompositeKey subTaskKey, String subTask, String subTaskResult, boolean processed) {
        this.subTaskKey = subTaskKey;
        this.subTask = subTask;
        this.subTaskResult = subTaskResult;
        this.processed = processed;
    }

    public TaskWorkerCompositeKey getSubTaskKey() {
        return subTaskKey;
    }

    public void setSubTaskKey(TaskWorkerCompositeKey subTaskKey) {
        this.subTaskKey = subTaskKey;
    }

    public String getSubTask() {
        return subTask;
    }

    public void setSubTask(String subTask) {
        this.subTask = subTask;
    }

    public String getSubTaskResult() {
        return subTaskResult;
    }

    public void setSubTaskResult(String subTaskResult) {
        this.subTaskResult = subTaskResult;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
