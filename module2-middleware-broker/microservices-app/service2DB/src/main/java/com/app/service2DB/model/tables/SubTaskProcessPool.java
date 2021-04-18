package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
public class SubTaskProcessPool {

    @PrimaryKey
    private UUID taskId;
    private LocalDateTime initiatedTime;

    public SubTaskProcessPool() {}

    public SubTaskProcessPool(UUID taskId, LocalDateTime initiatedTime) {
        this.taskId = taskId;
        this.initiatedTime = initiatedTime;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getInitiatedTime() {
        return initiatedTime;
    }

    public void setInitiatedTime(LocalDateTime initiatedTime) {
        this.initiatedTime = initiatedTime;
    }
}
