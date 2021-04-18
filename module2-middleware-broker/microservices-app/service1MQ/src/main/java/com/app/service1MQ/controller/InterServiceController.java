package com.app.service1MQ.controller;

import com.app.service1MQ.model.MessagingChannel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * A service class that provides methods to receive and send messages from other microservices.
 */
@Service
@EnableBinding(MessagingChannel.class)
public class InterServiceController {

    @Autowired
    private MessagingChannel channel;

    @Autowired
    private SubPubService subPubService;

    public InterServiceController() {}

    /**
     * Send the subscribed worker details that received from the workers to the Database microservice.
     * @param workerData subscribed workers details of type String.
     */
    public String publishWorkerDetails(String workerData) {

        Message<String> workerDetails = MessageBuilder.withPayload(workerData).build();
        channel.workerDetails().send(workerDetails);
        return "Worker details " + "has been sent to the DB micro service:\n" + workerDetails.toString() ;
    }

    /**
     * Send the task details that received from the task initiator(UI) to the Database microservice.
     * @param taskData task details of type JSON String.
     */
    public String publishTaskDetails(String taskData) {

        Message<String> taskDetails = MessageBuilder.withPayload(taskData).build();
        channel.taskDetails().send(taskDetails);
        return "Task details " + "has been sent to the DB micro service:\n" + taskDetails.toString() ;
    }

    /**
     * Receives task description details assigned to the workers from TE microservice
     * and sends received details to the workers through MQTT.
     * @param message the details of the assigned task to the workers as JSON string.
     * @throws JSONException throws any json related exceptions.
     */
    @StreamListener(target = "assigned_task")
    public void onMessageReceived(String message) throws JSONException {
        System.out.println(message);
        subPubService.publishTaskToWorker(message);
    }

    /**
     * Send the details of the workers who accepted the task to the Database microservice.
     * @param workerRes worker response as JSON string.
     */
    public void publishWorkerResponse(String workerRes) {

        Message<String> workerResponse = MessageBuilder.withPayload(workerRes).build();
        channel.workerResponse().send(workerResponse);
    }

    /**
     * Receives executable file(.jar or .apk) along with workers details from TE microservice.
     * and sends received executable task to the workers through MQTT.
     * @param message the details of the assigned executable subtask to the workers.
     * @throws JSONException throws any json related exceptions.
     */
    @StreamListener(target = "assigned_sub_task")
    public void onMessageArrival(String message) throws JSONException {
        subPubService.publishTaskToWorker(message);
    }

    /**
     * Receives executable subtask details assigned to the workers from TE microservice
     * and sends received details to the workers through MQTT.
     * @param result the details of the assigned executable subtask to the workers as JSON string.
     */
    public void publishSubTaskResult(String result) {

        Message<String> subTaskResult = MessageBuilder.withPayload(result).build();
        channel.subTaskResult().send(subTaskResult);
    }

    /**
     * Receives the accumulated subtask details from TE microservice and
     * sends the result to the task initiator through MQTT.
     * @param results the accumulated result of type JSON string.
     * @throws MqttException throws any MQTT related exceptions.
     */
    @StreamListener(target = "accumulated_results")
    public void onMessage(String results) throws MqttException {
        subPubService.publishAccumulatedResultsToUI(results);
    }
}