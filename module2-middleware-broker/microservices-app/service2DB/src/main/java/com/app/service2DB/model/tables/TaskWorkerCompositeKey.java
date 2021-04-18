package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.UUID;

@PrimaryKeyClass
public class TaskWorkerCompositeKey implements Serializable {


    @PrimaryKeyColumn(name = "taskId",ordinal = 0,type = PrimaryKeyType.PARTITIONED)
    private UUID taskId;

    @PrimaryKeyColumn(name="workerId", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private String workerId;

    public TaskWorkerCompositeKey(UUID taskId, String workerId) {
        this.taskId = taskId;
        this.workerId = workerId;
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
}
