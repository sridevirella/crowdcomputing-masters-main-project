package com.app.service1MQ.mqtt.callback;

import com.app.service1MQ.controller.InterServiceController;
import com.app.service1MQ.controller.SubPubService;
import com.app.service1MQ.model.TopicName;
import com.app.service1MQ.mqtt.connection.MqttConnection;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A class that implements mqtt callback to receive or publish messages.
 */
@Component
public class MqttCallBack implements MqttCallback {

    final private static Integer qos = 2;
    final private static boolean retained = false;

    @Autowired
    private MqttConnection mqttConn;
    @Autowired
    private InterServiceController serviceController;
    @Autowired
    private SubPubService subPub;
    
    /**
     * Invoked when a message received for the subscribed topics.
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws JSONException {

        String response;

       if(topic.equals(TopicName.WORKER_SUBSCRIPTION.getTopicName()) || topic.equals(TopicName.WORKER_UN_SUBSCRIPTION.getTopicName())) {

            response = serviceController.publishWorkerDetails(new String(mqttMessage.getPayload()));
            System.out.println(response);
       }
       else if(topic.equals(TopicName.INITIATOR_TASK.getTopicName())) {

           response = serviceController.publishTaskDetails(new String(mqttMessage.getPayload()));
           System.out.println(response);
       }
       else if(topic.matches(TopicName.WORKER_TASK_RESPONSE.getTopicName() + ".*$")) {
           serviceController.publishWorkerResponse(new String(mqttMessage.getPayload()));
       }
       else if(topic.matches(TopicName.WORKER_SUBTASK_RESPONSE.getTopicName() + ".*$")) {
           serviceController.publishSubTaskResult(new String(mqttMessage.getPayload()));
       }
    }

    /**
     * Invoked when there is a connection loss with the mqtt broker
     */
    @Override
    public void connectionLost(Throwable cause) {

        System.out.println("Connection lost..");
        System.out.println("reason:" + cause);
    }

    /**
     * Invoked on successful delivery of message to MQTT.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    /**
     * Close client connection with the MQTT.
     * @throws MqttException throws any mqtt related exceptions.
     */
    public void disconnect() throws MqttException {

        mqttConn.getMqttClientInstance().disconnect();
    }
}
