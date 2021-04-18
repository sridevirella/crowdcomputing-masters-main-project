package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table
public class AccumulatedResults {

    @PrimaryKey
    private UUID taskId;
    private String result;

    public AccumulatedResults() {}

    public AccumulatedResults(UUID taskId, String result) {
        this.taskId = taskId;
        this.result = result;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
