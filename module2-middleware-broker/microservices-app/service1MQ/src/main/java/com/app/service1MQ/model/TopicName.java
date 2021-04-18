package com.app.service1MQ.model;

/**
 * A Enum class to define the constants.
 */
public enum TopicName {

    MAIN_TOPIC("mcc/#"),
    WORKER_SUBSCRIPTION("mcc/worker/subscribe"),
    WORKER_UN_SUBSCRIPTION("mcc/worker/unsubscribe"),
    INITIATOR_TASK("mcc/initiator/task"),
    WORKER_TASK_RESPONSE("mcc/worker/task_response"),
    WORKER_SUBTASK_RESPONSE("mcc/worker/subtask_response"),
    ACCUMULATED_RESULT("mcc/initiator/result");

    private final String topicName;

    TopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

}
