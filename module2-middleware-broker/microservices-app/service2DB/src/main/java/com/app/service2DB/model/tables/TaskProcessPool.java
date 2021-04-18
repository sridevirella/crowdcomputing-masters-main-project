package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
public class TaskProcessPool {

    @PrimaryKey
    private UUID taskId;
    private LocalDateTime dueDate;
    private int sno;

    public TaskProcessPool() {}

    public TaskProcessPool(UUID taskId, LocalDateTime dueDate, int sno) {
        this.taskId = taskId;
        this.dueDate = dueDate;
        this.sno = sno;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }
}
