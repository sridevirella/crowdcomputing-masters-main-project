package com.app.service3te.model;

/**
 * A Enum class to define the constants.
 */
public enum Topic {

    WORKER_TASK("mcc/worker/taskDescription"),
    WORKER_TASK_RESPONSE("mcc/worker/task_response"),
    WORKER_SUBTASK("mcc/worker/subtask"),
    WORKER_SUBTASK_RESPONSE("mcc/worker/subtask_response");

    private final String topic;

    Topic(String topicName) {
        this.topic = topicName;
    }

    public String getTopicName() {
        return topic;
    }
}
