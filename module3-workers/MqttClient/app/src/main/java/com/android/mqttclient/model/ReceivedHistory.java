package com.android.mqttclient.model;

/**
 * Model class for received tasks.
 */
public class ReceivedHistory {
    private String task;
    private String timeStamp;
    private int duration;
    private String downloadFile;
    private boolean isFileReceived;

    public ReceivedHistory(String task, String time, int duration, String downloadFile,boolean isFileReceived) {
        this.task = task;
        this.timeStamp = time;
        this.duration = duration;
        this.downloadFile = downloadFile;
        this.isFileReceived = isFileReceived;
    }

    public String getTask() {
        return task;
    }

    public String getTime() {
        return timeStamp;
    }

    public int getDuration() {
        return duration;
    }

    public String getDownloadFile() {
        return downloadFile;
    }

    public boolean isFileReceived() {
        return isFileReceived;
    }
}
