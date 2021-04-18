package com.app.service1MQ.mqtt.connection;

import com.app.service1MQ.model.TopicName;
import com.app.service1MQ.mqtt.callback.MqttCallBack;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Class which establishes an MQTT server connection.
 */
@Service
public class MqttConnection {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("{$mqtt.client.id}")
    private String clientId;

    private IMqttClient mqttClientInstance;
    @Autowired
    private MqttCallBack mqttCallBack;

    /**
     * Setup MQtt client connection using different connect options and callbacks.
     */
    @PostConstruct
    public void mqttClientConnection() {

        try {
            clientConnection();
            MqttConnectOptions options = getMqttConnectOptions();
            connectWithOptions(options);

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an IMqttClient instance.
     * @throws MqttException throws any mqtt related exceptions.
     */
    private void clientConnection() throws MqttException {

        if ( mqttClientInstance == null )
            mqttClientInstance = new MqttClient( brokerUrl, clientId );
    }

    /**
     * MQTT connection options.
     * @param options mqtt connection options of type MqttConnectOptions.
     * @throws MqttException throws any mqtt related exceptions.
     */
    private void connectWithOptions(MqttConnectOptions options) throws MqttException {

        if (!mqttClientInstance.isConnected()) {
            mqttClientInstance.connect(options);
            System.out.println("connected to: "+ brokerUrl);
            mqttClientInstance.subscribe(TopicName.MAIN_TOPIC.getTopicName());
        }
    }

    /**
     * Create MQTT connection options.
     * @return connection options of type MqttConnectOptions.
     */
    private MqttConnectOptions getMqttConnectOptions() {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        mqttClientInstance.setCallback(mqttCallBack);
        return options;
    }

    /**
     * Getter for IMqttClient instance.
     * @return IMqttClient instance.
     */
    public IMqttClient getMqttClientInstance() {
        return mqttClientInstance;
    }
}
