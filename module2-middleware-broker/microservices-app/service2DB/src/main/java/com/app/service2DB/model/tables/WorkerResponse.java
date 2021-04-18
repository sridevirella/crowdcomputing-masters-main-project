package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public class WorkerResponse {


   @PrimaryKey
    private TaskWorkerCompositeKey responseKey;
    private String accepted;

    public WorkerResponse(TaskWorkerCompositeKey responseKey, String accepted) {
        this.responseKey = responseKey;
        this.accepted = accepted;
    }

    public TaskWorkerCompositeKey getResponseKey() {
        return responseKey;
    }

    public void setResponseKey(TaskWorkerCompositeKey responseKey) {
        this.responseKey = responseKey;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }
}
