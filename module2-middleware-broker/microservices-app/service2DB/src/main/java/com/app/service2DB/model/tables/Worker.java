package com.app.service2DB.model.tables;

import org.json.JSONString;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public class Worker {

    @PrimaryKey
    private String workerId;
    private String deviceOS;
    private Boolean isAvailable;

    public Worker() {}

    public Worker(String workerId, String deviceOS, Boolean isAvailable) {
        this.workerId = workerId;
        this.deviceOS = deviceOS;
        this.isAvailable = isAvailable;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String id) {
        this.workerId = id;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "{\"workerId\" :" + workerId +
                "{\"deviceOS\" :" + deviceOS +
                "{\"isAvailable\" :" + isAvailable;
    }
}
