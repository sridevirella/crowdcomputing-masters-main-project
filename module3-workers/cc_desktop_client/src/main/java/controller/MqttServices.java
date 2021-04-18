package controller;

import model.Constants;
import model.Message;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * {@link MqttServices} is a worker class which establishes an MQTT server connection using an instance of {@link IMqttClient}.
 * Provides {@link MqttServicesListener} interface to handle the message call backs.
 */
public class MqttServices implements MqttCallback {

    private IMqttClient mqttClientInstance;
    private MqttServicesListener listener;
    private connectionStatusListener connectionStatus;

    public MqttServices() {}

    /**
     * Initiates MQTT connection.
     * @throws MqttException throws any mqtt related exceptions.
     */
    public void initiateConnection() throws MqttException {
        mqttClientInstance = mqttConnection();
    }

    /**
     * Interface defined to handle {@link Message} whenever a message received from MQTT.
     */
    public interface MqttServicesListener {
        void onMessageReceived(Message message) throws JSONException, IOException, MqttException;
    }

    /**
     * Setter for {@link MqttServicesListener} instance.
     * @param listener : {@link MqttServicesListener}
     */
    public void setListener(MqttServicesListener listener){
        this.listener = listener;
    }

    /**
     * Interface defined to handle MQTT connection status.
     */
    public interface connectionStatusListener {
        void onStatusChange(String status);
    }

    /**
     * Setter for {@link connectionStatusListener} instance.
     * @param connectionStatusListener : {@link connectionStatusListener}
     */
    public void setConnectionStatusListener(connectionStatusListener connectionStatusListener){
        this.connectionStatus = connectionStatusListener;
    }

    /**
     * Invoked when there is a connection loss with the mqtt broker
     */
    @Override
    public void connectionLost(Throwable cause) {
        connectionStatus.onStatusChange("Connection lost with MQTT");
        System.out.println("Connection lost...\n"+ cause);
    }

    /**
     * Invoked when a message received for the subscribed topics.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        if( topic.equals(Constants.WORKER_TASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) )
        {
            if(listener != null){
                listener.onMessageReceived(new Message(new String(message.getPayload()), null ));
            }
        }

        else if( topic.equals(Constants.WORKER_SUBTASK.getConstant() + "/" + Constants.WORKER_ID.getConstant()) ) {
            if(listener != null){
                listener.onMessageReceived(new Message(null, message.getPayload() ));
            }
        }
    }

    /**
     * Invoked once message successfully deliver to mqtt
     * Upon successful delivery of task response, subscribe to sub task topic
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Message Delivered");
    }

    /**
     * subscribes to the topic with given QOS and message retained values.
     * @param topic : Topic name as {@link String}
     */
    public void subscribeToTopic(String topic) throws MqttException {

        if ( mqttClientInstance.isConnected() ) {
            mqttClientInstance.subscribe(topic, Integer.parseInt(Constants.QOS.getConstant()));
            System.out.println( "Subscribed to topic:" + topic );
        }
    }

    /**
     *Publish a message on the topic with given QOS and retained values.
     * @param topic : Topic name as {@link String}
     * @param message : Message related to the topic as {@link String}
     */
    public void publishMessage(String topic, String message) throws MqttException {

        MqttMessage mqttMessage = getMqttMessage(message);

        if(mqttClientInstance.isConnected())
            mqttClientInstance.publish(topic, mqttMessage);
        System.out.println("published message:"+message+"===== to the topic:"+topic);
    }

    /**
     * Create MqttMessage from String payload.
     * @param message is of type String.
     * @return a mqttMessage of type MqttMessage.
     */
    private MqttMessage getMqttMessage(String message) {

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(Integer.parseInt(Constants.QOS.getConstant()));
        mqttMessage.setRetained(Boolean.parseBoolean(Constants.RETAINED.getConstant()));
        mqttMessage.setPayload(message.getBytes());
        return mqttMessage;
    }

    /**
     * Setup MQtt android client connection using different connect options and callbacks.
     * @return a IMqttClient instance.
     * @throws MqttException throws any mqtt related exceptions.
     */
    private IMqttClient mqttConnection() throws MqttException {

        if ( mqttClientInstance == null ) {
            mqttClientInstance = new MqttClient( Constants.URI.getConstant(), Constants.WORKER_ID.getConstant() );
        }

        MqttConnectOptions options = getMqttConnectOptions();
        mqttClientInstance.setCallback( this );
        establishConnection(options);
        return mqttClientInstance;
    }

    /**
     * Establish a connection with MQTT.
     * @param options mqtt connection options.
     */
    private void establishConnection(MqttConnectOptions options) {

        if ( !mqttClientInstance.isConnected() ) {
            try {
                mqttClientInstance.connect(options);
                connectionStatus.onStatusChange("Successfully connected to MQTT\n\nTo receive tasks please subscribe through \n \"Subscribe\" menu option");
                System.out.println("connected to: " + Constants.URI.getConstant());
            } catch (Exception e) {
                connectionStatus.onStatusChange("Failed to connect MQTT");
            }
        }
    }

    /**
     * MQTT connection options.
     */
    private MqttConnectOptions getMqttConnectOptions() {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect( true );
        options.setCleanSession( false );
        options.setConnectionTimeout( 10 );
        return options;
    }

    /**
     * Close client connection with the MQTT.
     * @throws MqttException throws any mqtt related exceptions.
     */
    public void disconnect() throws MqttException {
        mqttClientInstance.disconnect();
    }

    /**
     * Construct JSON payload for worker subscribe and unsubscribe.
     * @param isAvailable is of type boolean that indicates user worker availability.
     * @return workerDetailsObj is of type string.
     * @throws JSONException throws any JSON related exception s.
     */
    private String getWorkerDetailsPayload(boolean isAvailable) throws JSONException {

        JSONObject workerDetailsObj = new JSONObject();
        workerDetailsObj.put("workerId", Constants.WORKER_ID.getConstant());
        workerDetailsObj.put("deviceOS", "Desktop");
        workerDetailsObj.put("isAvailable", isAvailable);
        return workerDetailsObj.toString();
    }

    /**
     * Subscribe to the main topic to receive tasks and publish worker availability to middleware broker.
     * @throws JSONException throws any JSON related exceptions.
     * @throws MqttException throws any MQTT related exceptions.
     */
    public void subscribeForTasks() throws JSONException, MqttException {

        subscribeToTopic(Constants.MAIN_TOPIC.getConstant());
        publishMessage(Constants.WORKER_SUBSCRIPTION.getConstant(), getWorkerDetailsPayload(true));
    }

    /**
     * Unsubscribe to stop receiving the tasks.
     * @throws JSONException throws any JSON related exceptions.
     * @throws MqttException throws any MQTT related exceptions.
     */
    public void unsubscribe() throws JSONException, MqttException {

        publishMessage(Constants.WORKER_UN_SUBSCRIPTION.getConstant(), getWorkerDetailsPayload(false));
    }
}