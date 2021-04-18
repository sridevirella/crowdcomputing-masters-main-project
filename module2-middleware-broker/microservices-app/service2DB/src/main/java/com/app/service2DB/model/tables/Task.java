package com.app.service2DB.model.tables;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table
public class Task {

    @PrimaryKey
    private UUID taskId;
    private String fileName;
    private String fileContent;
    private int fileSize;
    private String mimeType;
    private String shortName;
    private String description;
    private String dueDate;
    private String size;
    private String author;
    private String rewards;

    public Task() {}

    public Task(UUID taskId, String fileName, String fileContent, int fileSize, String mimeType, String shortName, String description, String dueDate, String size, String author, String rewards) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.shortName = shortName;
        this.description = description;
        this.dueDate = dueDate;
        this.size = size;
        this.author = author;
        this.rewards = rewards;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", fileName='" + fileName + '\'' +
                ", fileContent='" + fileContent + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", shortName='" + shortName + '\'' +
                ", description='" + description + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", size='" + size + '\'' +
                ", author='" + author + '\'' +
                ", rewards='" + rewards + '\'' +
                '}';
    }
}
