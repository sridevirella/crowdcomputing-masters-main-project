package com.app.service2DB.model.tables;

import java.util.UUID;

public class SubTaskResult {

    private UUID taskId;
    private String workerId;
    private String subTaskResult;

    public SubTaskResult(UUID taskId, String workerId, String subTaskResult) {
        this.taskId = taskId;
        this.workerId = workerId;
        this.subTaskResult = subTaskResult;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getSubTaskResult() {
        return subTaskResult;
    }

    public void setSubTaskResult(String subTaskResult) {
        this.subTaskResult = subTaskResult;
    }
}
