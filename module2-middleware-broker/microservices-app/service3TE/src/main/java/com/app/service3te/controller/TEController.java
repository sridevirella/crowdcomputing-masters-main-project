package com.app.service3te.controller;

import com.app.service3te.model.MessagingChannel;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A component class that provides methods to receive and send messages from other microservices.
 */
@Component
@EnableBinding({MessagingChannel.class, Sink.class})
public class TEController {

    @Autowired
    private MessagingChannel channel;
    @Autowired
    private TaskHandler taskHandler;

    /**
     * Assign the task description to all the subscribed workers who are ready to receive the task.
     * @param message subscribed workers and task details as JSON string.
     * @throws JSONException throw any JSON related exceptions.
     * @throws IOException throw any IO related exceptions.
     */
    @StreamListener(target = "available_workers")
    public void onMessageReceived(String message) throws JSONException, IOException {

        System.out.println("received available workers and task description details");
        publishAssignedTask(taskHandler.getPayLoadForWorkers(message, false));
    }

    /**
     * Send the assigned task details to the MQ and DB microservices.
     * @param message assigned task details as JSON string.
     */
    private void publishAssignedTask(String message) {

        Message<String> msg = MessageBuilder.withPayload(message).build();
        channel.publishAssignedTask().send(msg);
    }

    /**
     * Divide the task into multiple executable subtasks based on the workers who agreed to run the task on their device.
     * @param message task accepted workers and executable file/task details as JSON string.
     * @throws JSONException throw any JSON related exceptions.
     * @throws IOException throw any IO related exceptions.
     */
    @StreamListener(target = "subtask_accepted_workers")
    public void onMessage(String message) throws JSONException, IOException {

        System.out.println("received accepted workers and sub task details");
        publishAssignedSubTask(taskHandler.getPayLoadForWorkers(message, true));
    }

    /**
     * Send the assigned executable subtask to each worker to MQ and DB microservice.
     * @param message assigned subtask details as JSON string.
     */
    public void publishAssignedSubTask(String message) {

        Message<String> msg = MessageBuilder.withPayload(message).build();
        channel.publishAssignedSubTask().send(msg);
    }
}
