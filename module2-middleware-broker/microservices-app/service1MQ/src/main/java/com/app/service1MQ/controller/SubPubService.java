package com.app.service1MQ.controller;

import com.app.service1MQ.model.TopicName;
import com.app.service1MQ.mqtt.connection.MqttConnection;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Class provides subscribe, publish methods to send messages to MQTT.
 */
@Component
public class SubPubService {

    final private static Integer qos = 2;
    final private static boolean retained = false;

    @Autowired
    private MqttConnection mqttConn;

    /**
     * publish a message on the topic with given QOS and retained values.
     */
    public void publishMessage( MqttMessage mqttMessage,
                                String topic,
                                Integer qos,
                                boolean retained)
            throws MqttException {

        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        if ( mqttConn.getMqttClientInstance().isConnected() ) {
            mqttConn.getMqttClientInstance().publish(topic, mqttMessage);
            System.out.println("published message successfully");
        }
    }

    /**
     * Subscribe to the topic with specified QOS level.
     * retained values.
     */
    public void subscribeToTopic( String topic, Integer qos ) throws MqttException {

            if ( mqttConn.getMqttClientInstance().isConnected() ) {
                mqttConn.getMqttClientInstance().subscribe( topic, qos );
                System.out.println( "Subscribed to topic:" + topic );
            }
    }

    public void publishTaskToWorker(String message) throws JSONException {

        JSONObject jsonObject = new JSONObject(message);
        JSONArray jsonArray = jsonObject.getJSONArray("WorkerAndTask");

        IntStream.range(0, jsonArray.length()).forEach(i -> {

            try {
                JSONObject topicAndTaskJson = jsonArray.getJSONObject(i);
                System.out.println("Publish task to the worker\n"+topicAndTaskJson.toString());
                publishMessage(new MqttMessage(topicAndTaskJson.getJSONObject("task").toString().getBytes()), topicAndTaskJson.getString("workerTopic"), qos, retained);

            } catch (JSONException | MqttException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Publish the accumulated subtask results sent by the TE service to MQTT.
     * @param message accumulated subtask results type of String.
     * @throws MqttException throws any mqtt related exceptions.
     */
    public void publishAccumulatedResultsToUI(String message) throws MqttException {

        publishMessage(new MqttMessage(message.getBytes()), TopicName.ACCUMULATED_RESULT.getTopicName(), qos, retained);
    }
}
